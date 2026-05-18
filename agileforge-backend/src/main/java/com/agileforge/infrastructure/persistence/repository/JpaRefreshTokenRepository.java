package com.agileforge.infrastructure.persistence.repository;

import com.agileforge.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaRefreshTokenRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenAndRevokedFalse(String token);

    @Modifying
    @Query("UPDATE RefreshTokenEntity rt SET rt.revoked = true, rt.revokedAt = CURRENT_TIMESTAMP WHERE rt.userId = :userId AND rt.revoked = false")
    void revokeAllByUserId(@Param("userId") UUID userId);
}
