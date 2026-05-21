package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class UserBadge {

    private UUID id;
    private UUID userId;
    private UUID badgeId;
    private Instant earnedAt;
    private Badge badge;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public UUID getBadgeId() { return badgeId; }
    public void setBadgeId(UUID badgeId) { this.badgeId = badgeId; }
    public Instant getEarnedAt() { return earnedAt; }
    public void setEarnedAt(Instant earnedAt) { this.earnedAt = earnedAt; }
    public Badge getBadge() { return badge; }
    public void setBadge(Badge badge) { this.badge = badge; }
}
