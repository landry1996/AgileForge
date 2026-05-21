package com.agileforge.domain.model;

import java.util.UUID;

public class UserXp {

    private UUID id;
    private UUID userId;
    private String domain;
    private int xpPoints;
    private int level;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    public String getDomain() { return domain; }
    public void setDomain(String domain) { this.domain = domain; }
    public int getXpPoints() { return xpPoints; }
    public void setXpPoints(int xpPoints) { this.xpPoints = xpPoints; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
