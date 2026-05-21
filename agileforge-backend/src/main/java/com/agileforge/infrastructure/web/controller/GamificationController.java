package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.service.GamificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/gamification")
public class GamificationController {

    private final GamificationService gamificationService;

    public GamificationController(GamificationService gamificationService) {
        this.gamificationService = gamificationService;
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<?> getUserProfile(@PathVariable UUID userId) {
        return ResponseEntity.ok(gamificationService.getUserProfile(userId));
    }

    @GetMapping("/badges")
    public ResponseEntity<?> getAvailableBadges() {
        return ResponseEntity.ok(gamificationService.getAvailableBadges());
    }

    @GetMapping("/badges/{userId}")
    public ResponseEntity<?> getUserBadges(@PathVariable UUID userId) {
        return ResponseEntity.ok(gamificationService.getUserBadges(userId));
    }

    @GetMapping("/streaks/{userId}")
    public ResponseEntity<?> getUserStreaks(@PathVariable UUID userId) {
        return ResponseEntity.ok(gamificationService.getUserStreaks(userId));
    }

    @GetMapping("/xp/{userId}")
    public ResponseEntity<?> getUserXp(@PathVariable UUID userId) {
        return ResponseEntity.ok(gamificationService.getUserXp(userId));
    }

    @GetMapping("/leaderboard/{organizationId}")
    public ResponseEntity<?> getLeaderboard(@PathVariable UUID organizationId,
                                            @RequestParam(defaultValue = "WEEKLY") String period) {
        return ResponseEntity.ok(gamificationService.getLeaderboard(organizationId, period));
    }

    @PostMapping("/leaderboard/opt-in")
    public ResponseEntity<Void> optInLeaderboard(@RequestBody Map<String, Object> request) {
        UUID userId = UUID.fromString((String) request.get("userId"));
        UUID orgId = UUID.fromString((String) request.get("organizationId"));
        boolean optIn = (boolean) request.get("optIn");
        gamificationService.optInLeaderboard(userId, orgId, optIn);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/achievements/{projectId}")
    public ResponseEntity<?> getTeamAchievements(@PathVariable UUID projectId) {
        return ResponseEntity.ok(gamificationService.getTeamAchievements(projectId));
    }

    @PostMapping("/activity")
    public ResponseEntity<Void> recordActivity(@RequestBody Map<String, Object> request) {
        UUID userId = UUID.fromString((String) request.get("userId"));
        String activityType = (String) request.get("activityType");
        @SuppressWarnings("unchecked")
        Map<String, Object> metadata = (Map<String, Object>) request.getOrDefault("metadata", Map.of());
        gamificationService.recordActivity(userId, activityType, metadata);
        return ResponseEntity.ok().build();
    }
}
