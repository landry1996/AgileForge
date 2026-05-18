package com.agileforge.domain.model;

import java.time.Instant;
import java.util.UUID;

public class RefreshToken {

    private UUID id;
    private UUID userId;
    private String token;
    private Instant expiresAt;
    private boolean revoked;
    private Instant revokedAt;
    private Instant createdAt;
    private String ipAddress;
    private String userAgent;

    public RefreshToken() {}

    public RefreshToken(UUID userId, String token, Instant expiresAt, String ipAddress, String userAgent) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.revoked = false;
        this.createdAt = Instant.now();
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean isValid() {
        return !revoked && !isExpired();
    }

    public void revoke() {
        this.revoked = true;
        this.revokedAt = Instant.now();
    }

    // Getters and Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public Instant getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

    public boolean isRevoked() { return revoked; }
    public void setRevoked(boolean revoked) { this.revoked = revoked; }

    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(Instant revokedAt) { this.revokedAt = revokedAt; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
}
