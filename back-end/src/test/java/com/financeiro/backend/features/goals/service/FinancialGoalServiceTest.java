package com.financeiro.backend.features.goals.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.AccessDeniedException;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.goals.dto.CreateGoalRequest;
import com.financeiro.backend.features.goals.dto.UpdateGoalProgressRequest;
import com.financeiro.backend.features.goals.entity.FinancialGoal;
import com.financeiro.backend.features.goals.enums.GoalStatus;
import com.financeiro.backend.features.goals.event.GoalCompletedEvent;
import com.financeiro.backend.features.goals.event.GoalCreatedEvent;
import com.financeiro.backend.features.goals.repository.FinancialGoalRepository;
import com.financeiro.backend.features.wallet.entity.Wallet;
import com.financeiro.backend.features.wallet.repository.WalletRepository;
import com.financeiro.backend.features.wallet.service.WalletAccessService;

class FinancialGoalServiceTest {
    private final FinancialGoalRepository goals = mock(FinancialGoalRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final WalletRepository wallets = mock(WalletRepository.class);
    private final WalletAccessService access = mock(WalletAccessService.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final FinancialGoalService service = new FinancialGoalService(goals, users, wallets, access, events);
    private UUID userId;
    private UUID walletId;
    private User user;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        walletId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
        wallet = Wallet.builder().id(walletId).owner(user).name("Reserva").currency("BRL").build();
    }

    @Test
    void createsGoalOnlyAfterWalletAuthorizationAndPublishesEvent() {
        var request = new CreateGoalRequest(walletId, "Reserva de emergência", new BigDecimal("5000"),
                new BigDecimal("250"), LocalDate.now().plusMonths(6));
        when(wallets.findById(walletId)).thenReturn(Optional.of(wallet));
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(goals.save(any())).thenAnswer(invocation -> {
            FinancialGoal goal = invocation.getArgument(0);
            goal.setId(UUID.randomUUID());
            return goal;
        });

        var response = service.create(request, userId);

        verify(access).requireView(wallet, userId);
        verify(events).publishEvent(any(GoalCreatedEvent.class));
        verify(events, never()).publishEvent(any(GoalCompletedEvent.class));
        assertEquals(GoalStatus.ACTIVE, response.status());
        assertEquals(5, response.progressPercentage());
    }

    @Test
    void completesGoalOnceAndPublishesCompletionEvent() {
        UUID goalId = UUID.randomUUID();
        var goal = activeGoal(goalId);
        when(goals.findByIdAndUserId(goalId, userId)).thenReturn(Optional.of(goal));
        when(goals.save(goal)).thenReturn(goal);

        var response = service.updateProgress(goalId, new UpdateGoalProgressRequest(new BigDecimal("1500")), userId);

        assertEquals(GoalStatus.COMPLETED, response.status());
        assertEquals(new BigDecimal("1000"), response.currentAmount());
        assertEquals(100, response.progressPercentage());
        verify(events).publishEvent(new GoalCompletedEvent(userId, goalId));

        assertThrows(IllegalArgumentException.class,
                () -> service.updateProgress(goalId, new UpdateGoalProgressRequest(new BigDecimal("1000")), userId));
    }

    @Test
    void deniedWalletCannotBeUsedToCreateGoal() {
        var request = new CreateGoalRequest(walletId, "Sem acesso", BigDecimal.TEN, BigDecimal.ZERO, null);
        when(wallets.findById(walletId)).thenReturn(Optional.of(wallet));
        doThrow(new AccessDeniedException("Sem acesso")).when(access).requireView(wallet, userId);

        assertThrows(AccessDeniedException.class, () -> service.create(request, userId));

        verify(users, never()).findById(any());
        verify(goals, never()).save(any());
        verify(events, never()).publishEvent((Object) any());
    }

    private FinancialGoal activeGoal(UUID goalId) {
        return FinancialGoal.builder().id(goalId).user(user).wallet(wallet).name("Notebook")
                .targetAmount(new BigDecimal("1000")).currentAmount(new BigDecimal("500"))
                .status(GoalStatus.ACTIVE).createdAt(LocalDateTime.now()).updatedAt(LocalDateTime.now()).build();
    }
}
