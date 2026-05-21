package com.agileforge.application.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GamificationService Tests")
class GamificationServiceTest {

    private final GamificationService service = new GamificationService();

    @Test
    @DisplayName("Should return user profile with default values")
    void shouldReturnUserProfile() {
        UUID userId = UUID.randomUUID();
        var profile = service.getUserProfile(userId);

        assertThat(profile.userId()).isEqualTo(userId);
        assertThat(profile.level()).isEqualTo(1);
        assertThat(profile.totalXp()).isEqualTo(0);
        assertThat(profile.badges()).isEmpty();
        assertThat(profile.streaks()).isEmpty();
    }

    @Test
    @DisplayName("Should return empty badges list")
    void shouldReturnEmptyBadges() {
        assertThat(service.getUserBadges(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Should return empty streaks list")
    void shouldReturnEmptyStreaks() {
        assertThat(service.getUserStreaks(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Should return empty XP list")
    void shouldReturnEmptyXp() {
        assertThat(service.getUserXp(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Should return empty leaderboard")
    void shouldReturnEmptyLeaderboard() {
        assertThat(service.getLeaderboard(UUID.randomUUID(), "WEEKLY")).isEmpty();
    }

    @Test
    @DisplayName("Should return empty team achievements")
    void shouldReturnEmptyAchievements() {
        assertThat(service.getTeamAchievements(UUID.randomUUID())).isEmpty();
    }

    @Test
    @DisplayName("Should record activity without error")
    void shouldRecordActivity() {
        UUID userId = UUID.randomUUID();
        service.recordActivity(userId, "TICKET_COMPLETED", Map.of("ticketId", UUID.randomUUID().toString()));
    }

    @Test
    @DisplayName("Should opt in leaderboard without error")
    void shouldOptInLeaderboard() {
        service.optInLeaderboard(UUID.randomUUID(), UUID.randomUUID(), true);
    }

    @Test
    @DisplayName("Should celebrate milestone without error")
    void shouldCelebrateMilestone() {
        service.celebrateMilestone(UUID.randomUUID(), "Sprint Perfect", java.util.List.of(UUID.randomUUID()));
    }
}
