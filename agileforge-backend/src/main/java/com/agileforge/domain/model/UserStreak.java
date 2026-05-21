package com.agileforge.domain.model;

import java.time.LocalDate;
import java.util.UUID;

public class UserStreak {

    private UUID id;
    private UUID userId;
    private String streakType;
    private int currentCount;
    private int longestCount;
    private LocalDate lastActivityDate;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getStreakType() { return streakType; }
    public void setStreakType(String streakType) { this.streakType = streakType; }
    public int getCurrentCount() { return currentCount; }
    public void setCurrentCount(int currentCount) { this.currentCount = currentCount; }
    public int getLongestCount() { return longestCount; }
    public void setLongestCount(int longestCount) { this.longestCount = longestCount; }
    public LocalDate getLastActivityDate() { return lastActivityDate; }
    public void setLastActivityDate(LocalDate lastActivityDate) { this.lastActivityDate = lastActivityDate; }
}
