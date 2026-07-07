package com.diabecare.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 30)
    private String role;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Column(name = "terms_accepted_at")
    private LocalDateTime termsAcceptedAt;

    @Column(name = "terms_version", length = 20)
    private String termsVersion;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public boolean isSuspended() {
        return suspendedAt != null;
    }

    public boolean isDeleted() {
        return deletedAt != null;
    }
}