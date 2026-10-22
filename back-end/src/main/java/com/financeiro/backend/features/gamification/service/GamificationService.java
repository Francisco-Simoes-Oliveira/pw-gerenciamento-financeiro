package com.financeiro.backend.features.gamification.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.gamification.dto.AchievementResponse;
import com.financeiro.backend.features.gamification.dto.GamificationProfileResponse;
import com.financeiro.backend.features.gamification.dto.XpHistoryResponse;
import com.financeiro.backend.features.gamification.entity.GamificationProfile;
import com.financeiro.backend.features.gamification.entity.UserAchievement;
import com.financeiro.backend.features.gamification.entity.XpEvent;
import com.financeiro.backend.features.gamification.enums.AchievementCode;
import com.financeiro.backend.features.gamification.enums.XpEventType;
import com.financeiro.backend.features.gamification.repository.GamificationProfileRepository;
import com.financeiro.backend.features.gamification.repository.UserAchievementRepository;
import com.financeiro.backend.features.gamification.repository.XpEventRepository;

@Service
public class GamificationService {
    private final UserRepository users;
    private final GamificationProfileRepository profiles;
    private final XpEventRepository xpEvents;
    private final UserAchievementRepository userAchievements;
    private final LevelCalculator levels;

    public GamificationService(UserRepository users, GamificationProfileRepository profiles,
            XpEventRepository xpEvents, UserAchievementRepository userAchievements, LevelCalculator levels) {
        this.users = users;
        this.profiles = profiles;
        this.xpEvents = xpEvents;
        this.userAchievements = userAchievements;
        this.levels = levels;
    }

    @Transactional
    public GamificationProfileResponse getProfile(UUID userId) {
        long totalXp = profiles.findByUserId(userId).map(GamificationProfile::getTotalXp).orElse(0L);
        var progress = levels.calculate(totalXp);
        Map<AchievementCode, UserAchievement> unlocked = userAchievements
                .findByUserIdOrderByUnlockedAtDesc(userId).stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementCode, Function.identity()));

        var achievements = Arrays.stream(AchievementCode.values()).map(code -> {
            UserAchievement value = unlocked.get(code);
            return new AchievementResponse(code, code.getTitle(), code.getDescription(), value != null,
                    value == null ? null : value.getUnlockedAt());
        }).toList();

        var history = xpEvents.findTop10ByUserIdOrderByOccurredAtDesc(userId).stream()
                .map(event -> new XpHistoryResponse(event.getType(), event.getType().getLabel(), event.getXp(),
                        event.getOccurredAt()))
                .toList();

        return new GamificationProfileResponse(totalXp, progress.level(), progress.rank(),
                progress.currentLevelXp(), progress.xpPerLevel(), progress.nextLevelTotalXp(),
                progress.progressPercentage(), achievements, history);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public AwardResult award(UUID userId, XpEventType type, String referenceId) {
        if (userId == null || type == null) return AwardResult.notAwarded();
        String key = idempotencyKey(type, referenceId);
        if (xpEvents.existsByUserIdAndIdempotencyKey(userId, key)) return AwardResult.notAwarded();

        var user = users.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado para conceder XP."));
        LocalDateTime now = LocalDateTime.now();
        var profile = profiles.findByUserId(userId).orElseGet(() -> GamificationProfile.builder()
                .user(user).totalXp(0).createdAt(now).updatedAt(now).build());

        xpEvents.save(XpEvent.builder().user(user).type(type).xp(type.getXp()).idempotencyKey(key)
                .referenceId(referenceId).occurredAt(now).build());
        profile.addXp(type.getXp(), now);
        profiles.save(profile);

        AchievementCode unlocked = AchievementCode.forEvent(type)
                .filter(code -> !userAchievements.existsByUserIdAndAchievementCode(userId, code))
                .map(code -> {
                    userAchievements.save(UserAchievement.builder().user(user).achievementCode(code)
                            .unlockedAt(now).build());
                    return code;
                }).orElse(null);

        return new AwardResult(true, type.getXp(), unlocked);
    }

    private String idempotencyKey(XpEventType type, String referenceId) {
        if (type.isOneTime()) return type.name();
        if (referenceId == null || referenceId.isBlank()) {
            throw new IllegalArgumentException("Eventos recorrentes de XP exigem uma referência.");
        }
        return type.name() + ":" + referenceId;
    }

    public record AwardResult(boolean awarded, int xpAwarded, AchievementCode unlockedAchievement) {
        static AwardResult notAwarded() {
            return new AwardResult(false, 0, null);
        }
    }
}
