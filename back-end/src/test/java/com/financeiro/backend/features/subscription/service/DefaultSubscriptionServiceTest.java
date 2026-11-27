package com.financeiro.backend.features.subscription.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.subscription.entity.SubscriptionPlan;
import com.financeiro.backend.features.subscription.entity.UserSubscription;
import com.financeiro.backend.features.subscription.enums.SubscriptionStatus;
import com.financeiro.backend.features.subscription.repository.SubscriptionPlanRepository;
import com.financeiro.backend.features.subscription.repository.UserSubscriptionRepository;

@ExtendWith(MockitoExtension.class)
class DefaultSubscriptionServiceTest {

    @Mock
    private UserSubscriptionRepository subscriptionRepository;

    @Mock
    private SubscriptionPlanRepository planRepository;

    @InjectMocks
    private DefaultSubscriptionService service;

    @Test
    void createsFreeSubscriptionForNewUser() {
        User user = new User();
        user.setId(UUID.randomUUID());

        when(subscriptionRepository.findByUserId(user.getId())).thenReturn(Optional.empty());
        when(planRepository.findFirstByNameIgnoreCase("Gratuito")).thenReturn(Optional.empty());
        when(planRepository.save(any(SubscriptionPlan.class))).thenAnswer(i -> i.getArgument(0));
        when(subscriptionRepository.save(any(UserSubscription.class))).thenAnswer(i -> i.getArgument(0));

        UserSubscription subscription = service.ensureSubscription(user);

        assertSame(user, subscription.getUser());
        assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
        assertNull(subscription.getEndDate());
        assertEquals("Gratuito", subscription.getPlan().getName());
        assertEquals(BigDecimal.ZERO, subscription.getPlan().getPrice());
        assertEquals(3, subscription.getPlan().getMaxWallets());
    }

    @Test
    void doesNotReplaceAnExistingSubscription() {
        User user = new User();
        user.setId(UUID.randomUUID());
        UserSubscription existing = new UserSubscription();
        when(subscriptionRepository.findByUserId(user.getId())).thenReturn(Optional.of(existing));

        assertSame(existing, service.ensureSubscription(user));
        verify(planRepository, never()).save(any(SubscriptionPlan.class));
        verify(subscriptionRepository, never()).save(any(UserSubscription.class));
    }
}
