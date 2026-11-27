package com.financeiro.backend.features.subscription.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.subscription.entity.SubscriptionPlan;
import com.financeiro.backend.features.subscription.entity.UserSubscription;
import com.financeiro.backend.features.subscription.enums.SubscriptionStatus;
import com.financeiro.backend.features.subscription.repository.SubscriptionPlanRepository;
import com.financeiro.backend.features.subscription.repository.UserSubscriptionRepository;

/**
 * Garante que o usuário tenha um plano inicial sem exigir pagamento.
 * Também recupera contas criadas antes de existir o plano gratuito.
 */
@Service
public class DefaultSubscriptionService {

    private static final String FREE_PLAN_NAME = "Gratuito";

    private final UserSubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;

    public DefaultSubscriptionService(UserSubscriptionRepository subscriptionRepository,
                                      SubscriptionPlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }

    @Transactional
    public UserSubscription ensureSubscription(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Usuário precisa estar salvo antes da assinatura.");
        }

        return subscriptionRepository.findByUserId(user.getId()).orElseGet(() -> {
            SubscriptionPlan freePlan = planRepository.findFirstByNameIgnoreCase(FREE_PLAN_NAME)
                    .orElseGet(() -> planRepository.save(SubscriptionPlan.builder()
                            .name(FREE_PLAN_NAME)
                            .description("Plano gratuito inicial para uso do FinShare")
                            .price(BigDecimal.ZERO)
                            .maxWallets(3)
                            .maxMembersPerWallet(5)
                            .maxCategories(20)
                            .active(true)
                            .displayOrder(0)
                            .build()));

            // Nunca conceder por engano um plano pago com o nome reservado.
            if (freePlan.getPrice() == null || freePlan.getPrice().compareTo(BigDecimal.ZERO) != 0) {
                throw new IllegalStateException("O plano Gratuito deve ter preço zero.");
            }

            UserSubscription subscription = UserSubscription.builder()
                    .user(user)
                    .plan(freePlan)
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(LocalDate.now())
                    .endDate(null) // Gratuito: sem prazo de expiração.
                    .autoRenew(false)
                    .build();
            return subscriptionRepository.save(subscription);
        });
    }
}
