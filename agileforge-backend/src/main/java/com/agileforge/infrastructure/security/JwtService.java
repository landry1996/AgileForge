package com.agileforge.infrastructure.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class);

    private final SecretKey signingKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtService(
            @Value("${agileforge.security.jwt.secret-key}") String secretKey,
            @Value("${agileforge.security.jwt.access-token-expiration}") long accessTokenExpiration,
            @Value("${agileforge.security.jwt.refresh-token-expiration}") long refreshTokenExpiration) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
        this.accessTokenExpiration = accessTokenExpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String generateAccessToken(UUID userId, String email, Map<String, Object> extraClaims) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .claims(extraClaims)
                .issuedAt(Date.from(Instant.now()))
                .expiration(Date.from(Instant.now().plusMillis(accessTokenExpiration)))
                .signWith(signingKey)
                .compact();
    }

    public String generateRefreshTokenValue() {
        return UUID.randomUUID().toString();
    }

    public long getRefreshTokenExpiration() {
        return refreshTokenExpiration;
    }

    public long getAccessTokenExpiration() {
        return accessTokenExpiration;
    }

    public UUID extractUserId(String token) {
        Claims claims = extractClaims(token);
        return UUID.fromString(claims.getSubject());
    }

    public String extractEmail(String token) {
        Claims claims = extractClaims(token);
        return claims.get("email", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
