package com.financeiro.backend.features.gamification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.gamification.entity.GamificationProfile;
import com.financeiro.backend.features.gamification.entity.UserAchievement;
import com.financeiro.backend.features.gamification.entity.XpEvent;
import com.financeiro.backend.features.gamification.enums.AchievementCode;
import com.financeiro.backend.features.gamification.enums.XpEventType;
import com.financeiro.backend.features.gamification.repository.GamificationProfileRepository;
import com.financeiro.backend.features.gamification.repository.UserAchievementRepository;
import com.financeiro.backend.features.gamification.repository.XpEventRepository;

class GamificationServiceTest {
    private final UserRepository users = mock(UserRepository.class);
    private final GamificationProfileRepository profiles = mock(GamificationProfileRepository.class);
    private final XpEventRepository xpEvents = mock(XpEventRepository.class);
    private final UserAchievementRepository achievements = mock(UserAchievementRepository.class);
    private final GamificationService service = new GamificationService(users, profiles, xpEvents, achievements,
            new LevelCalculator());
    private UUID userId;
    private User user;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = new User();
        user.setId(userId);
    }

    @Test
    void awardsXpAndUnlocksMappedAchievement() {
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(profiles.findByUserId(userId)).thenReturn(Optional.empty());
        when(achievements.existsByUserIdAndAchievementCode(userId, AchievementCode.FIRST_STEP)).thenReturn(false);

        var result = service.award(userId, XpEventType.FIRST_TRANSACTION, UUID.randomUUID().toString());

        assertTrue(result.awarded());
        assertEquals(20, result.xpAwarded());
        assertEquals(AchievementCode.FIRST_STEP, result.unlockedAchievement());
        var profile = ArgumentCaptor.forClass(GamificationProfile.class);
        verify(profiles).save(profile.capture());
        assertEquals(20, profile.getValue().getTotalXp());
        var event = ArgumentCaptor.forClass(XpEvent.class);
        verify(xpEvents).save(event.capture());
        assertEquals("FIRST_TRANSACTION", event.getValue().getIdempotencyKey());
        verify(achievements).save(any(UserAchievement.class));
    }

    @Test
    void ignoresAnEventThatAlreadyUsedItsIdempotencyKey() {
        when(xpEvents.existsByUserIdAndIdempotencyKey(userId, "FIRST_WALLET")).thenReturn(true);

        var result = service.award(userId, XpEventType.FIRST_WALLET, UUID.randomUUID().toString());

        assertFalse(result.awarded());
        assertEquals(0, result.xpAwarded());
        verify(users, never()).findById(any());
        verify(profiles, never()).save(any());
        verify(xpEvents, never()).save(any());
    }

    @Test
    void recurringGoalAwardsUseTheGoalAsReference() {
        UUID goalId = UUID.randomUUID();
        when(users.findById(userId)).thenReturn(Optional.of(user));
        when(profiles.findByUserId(userId)).thenReturn(Optional.empty());

        var result = service.award(userId, XpEventType.GOAL_COMPLETED, goalId.toString());

        assertTrue(result.awarded());
        var event = ArgumentCaptor.forClass(XpEvent.class);
        verify(xpEvents).save(event.capture());
        assertEquals("GOAL_COMPLETED:" + goalId, event.getValue().getIdempotencyKey());
        assertEquals(200, event.getValue().getXp());
    }

    @Test
    void returnsAZeroProfileWithTheFullLockedCatalog() {
        when(profiles.findByUserId(userId)).thenReturn(Optional.empty());
        when(achievements.findByUserIdOrderByUnlockedAtDesc(userId)).thenReturn(List.of());
        when(xpEvents.findTop10ByUserIdOrderByOccurredAtDesc(userId)).thenReturn(List.of());

        var response = service.getProfile(userId);

        assertEquals(0, response.totalXp());
        assertEquals(1, response.level());
        assertEquals(AchievementCode.values().length, response.achievements().size());
        assertTrue(response.achievements().stream().noneMatch(item -> item.unlocked()));
    }
}
