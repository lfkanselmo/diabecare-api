package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenJpaRepository extends JpaRepository<RefreshTokenEntity, UUID> {

    Optional<RefreshTokenEntity> findByTokenHash(String tokenHash);

    @Query("SELECT r FROM RefreshTokenEntity r WHERE r.userId = :userId " +
            "AND r.revokedAt IS NULL AND r.expiresAt > CURRENT_TIMESTAMP " +
            "ORDER BY r.lastUsedAt DESC NULLS LAST, r.createdAt DESC")
    List<RefreshTokenEntity> findActiveByUserId(@Param("userId") UUID userId);

    @Modifying
    @Query("UPDATE RefreshTokenEntity r SET r.revokedAt = :revokedAt WHERE r.userId = :userId AND r.revokedAt IS NULL")
    void revokeAllActiveByUserId(@Param("userId") UUID userId, @Param("revokedAt") LocalDateTime revokedAt);

    void deleteByUserId(UUID userId);
}