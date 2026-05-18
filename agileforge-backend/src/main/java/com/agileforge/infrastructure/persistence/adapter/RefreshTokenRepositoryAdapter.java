package com.agileforge.infrastructure.persistence.adapter;

import com.agileforge.domain.model.RefreshToken;
import com.agileforge.domain.port.out.RefreshTokenRepositoryPort;
import com.agileforge.infrastructure.persistence.entity.RefreshTokenEntity;
import com.agileforge.infrastructure.persistence.repository.JpaRefreshTokenRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Component
public class RefreshTokenRepositoryAdapter implements RefreshTokenRepositoryPort {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

    public RefreshTokenRepositoryAdapter(JpaRefreshTokenRepository jpaRefreshTokenRepository) {
        this.jpaRefreshTokenRepository = jpaRefreshTokenRepository;
    }

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        RefreshTokenEntity entity = toEntity(refreshToken);
        RefreshTokenEntity saved = jpaRefreshTokenRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRefreshTokenRepository.findByTokenAndRevokedFalse(token)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public void revokeAllByUserId(UUID userId) {
        jpaRefreshTokenRepository.revokeAllByUserId(userId);
    }

    private RefreshTokenEntity toEntity(RefreshToken domain) {
        RefreshTokenEntity entity = new RefreshTokenEntity();
        entity.setId(domain.getId());
        entity.setUserId(domain.getUserId());
        entity.setToken(domain.getToken());
        entity.setExpiresAt(domain.getExpiresAt());
        entity.setRevoked(domain.isRevoked());
        entity.setRevokedAt(domain.getRevokedAt());
        entity.setCreatedAt(domain.getCreatedAt());
        entity.setIpAddress(domain.getIpAddress());
        entity.setUserAgent(domain.getUserAgent());
        return entity;
    }

    private RefreshToken toDomain(RefreshTokenEntity entity) {
        RefreshToken domain = new RefreshToken();
        domain.setId(entity.getId());
        domain.setUserId(entity.getUserId());
        domain.setToken(entity.getToken());
        domain.setExpiresAt(entity.getExpiresAt());
        domain.setRevoked(entity.isRevoked());
        domain.setRevokedAt(entity.getRevokedAt());
        domain.setCreatedAt(entity.getCreatedAt());
        domain.setIpAddress(entity.getIpAddress());
        domain.setUserAgent(entity.getUserAgent());
        return domain;
    }
}
