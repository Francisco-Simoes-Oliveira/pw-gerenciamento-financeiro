package com.financeiro.backend.features.goals.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.goals.dto.CreateGoalRequest;
import com.financeiro.backend.features.goals.dto.GoalResponse;
import com.financeiro.backend.features.goals.dto.UpdateGoalProgressRequest;
import com.financeiro.backend.features.goals.dto.UpdateGoalRequest;
import com.financeiro.backend.features.goals.entity.FinancialGoal;
import com.financeiro.backend.features.goals.enums.GoalStatus;
import com.financeiro.backend.features.goals.event.GoalCompletedEvent;
import com.financeiro.backend.features.goals.event.GoalCreatedEvent;
import com.financeiro.backend.features.goals.repository.FinancialGoalRepository;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;

@Service
public class FinancialGoalService {
    private final FinancialGoalRepository goals;
    private final UserRepository users;
    private final WalletRepository wallets;
    private final WalletAccessService access;
    private final ApplicationEventPublisher events;

    public FinancialGoalService(FinancialGoalRepository goals, UserRepository users, WalletRepository wallets,
            WalletAccessService access, ApplicationEventPublisher events) {
        this.goals = goals;
        this.users = users;
        this.wallets = wallets;
        this.access = access;
        this.events = events;
    }

    @Transactional(readOnly = true)
    public List<GoalResponse> list(UUID userId) {
        return goals.findByUserIdOrderByCreatedAtDesc(userId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public GoalResponse find(UUID id, UUID userId) {
        return toResponse(findOwned(id, userId));
    }

    @Transactional
    public GoalResponse create(CreateGoalRequest request, UUID userId) {
        validateDeadline(request.deadline());
        var wallet = wallets.findById(request.walletId())
                .orElseThrow(() -> new ResourceNotFoundException("Carteira não encontrada."));
        access.requireView(wallet, userId);
        var user = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        LocalDateTime now = LocalDateTime.now();
        BigDecimal current = request.currentAmount() == null ? BigDecimal.ZERO : request.currentAmount();
        boolean completed = current.compareTo(request.targetAmount()) >= 0;

        var goal = goals.save(FinancialGoal.builder()
                .user(user).wallet(wallet).name(request.name().trim()).targetAmount(request.targetAmount())
                .currentAmount(completed ? request.targetAmount() : current)
                .deadline(request.deadline()).status(completed ? GoalStatus.COMPLETED : GoalStatus.ACTIVE)
                .createdAt(now).updatedAt(now).completedAt(completed ? now : null).build());

        events.publishEvent(new GoalCreatedEvent(userId, goal.getId()));
        if (completed) events.publishEvent(new GoalCompletedEvent(userId, goal.getId()));
        return toResponse(goal);
    }

    @Transactional
    public GoalResponse update(UUID id, UpdateGoalRequest request, UUID userId) {
        validateDeadline(request.deadline());
        var goal = requireActive(findOwned(id, userId));
        goal.setName(request.name().trim());
        goal.setTargetAmount(request.targetAmount());
        goal.setDeadline(request.deadline());
        completeIfReached(goal, userId);
        goal.setUpdatedAt(LocalDateTime.now());
        return toResponse(goals.save(goal));
    }

    @Transactional
    public GoalResponse updateProgress(UUID id, UpdateGoalProgressRequest request, UUID userId) {
        var goal = requireActive(findOwned(id, userId));
        goal.setCurrentAmount(request.currentAmount());
        completeIfReached(goal, userId);
        goal.setUpdatedAt(LocalDateTime.now());
        return toResponse(goals.save(goal));
    }

    @Transactional
    public void cancel(UUID id, UUID userId) {
        var goal = requireActive(findOwned(id, userId));
        goal.setStatus(GoalStatus.CANCELED);
        goal.setUpdatedAt(LocalDateTime.now());
        goals.save(goal);
    }

    private void completeIfReached(FinancialGoal goal, UUID userId) {
        if (goal.getCurrentAmount().compareTo(goal.getTargetAmount()) >= 0) {
            goal.setCurrentAmount(goal.getTargetAmount());
            goal.setStatus(GoalStatus.COMPLETED);
            goal.setCompletedAt(LocalDateTime.now());
            events.publishEvent(new GoalCompletedEvent(userId, goal.getId()));
        }
    }

    private FinancialGoal requireActive(FinancialGoal goal) {
        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new IllegalArgumentException("Somente metas ativas podem ser alteradas.");
        }
        return goal;
    }

    private FinancialGoal findOwned(UUID id, UUID userId) {
        return goals.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Meta não encontrada."));
    }

    private void validateDeadline(LocalDate deadline) {
        if (deadline != null && deadline.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("O prazo da meta não pode estar no passado.");
        }
    }

    private GoalResponse toResponse(FinancialGoal goal) {
        int progress = goal.getTargetAmount().signum() == 0 ? 0 : goal.getCurrentAmount()
                .multiply(BigDecimal.valueOf(100)).divide(goal.getTargetAmount(), 0, RoundingMode.DOWN)
                .min(BigDecimal.valueOf(100)).intValue();
        return new GoalResponse(goal.getId(), goal.getWallet().getId(), goal.getWallet().getName(),
                goal.getWallet().getCurrency() == null ? "BRL" : goal.getWallet().getCurrency(), goal.getName(),
                goal.getTargetAmount(), goal.getCurrentAmount(), progress, goal.getDeadline(), goal.getStatus(),
                goal.getCreatedAt(), goal.getCompletedAt());
    }
}
