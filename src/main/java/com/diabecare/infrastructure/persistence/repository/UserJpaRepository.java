package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserJpaRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmail(String email);
    boolean existsByEmail(String email);

    @Query("SELECT u.id FROM UserEntity u WHERE u.deletedAt IS NOT NULL AND u.deletedAt < :cutoff")
    List<UUID> findIdsDeletedBefore(@Param("cutoff") LocalDateTime cutoff);
}
