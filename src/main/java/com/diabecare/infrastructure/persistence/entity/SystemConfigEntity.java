package com.diabecare.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "system_config")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemConfigEntity {

    @Id
    @Column(length = 100)
    private String key;

    @Column(nullable = false)
    private String value;

    @Column(name = "data_type", nullable = false, length = 20)
    private String dataType;

    @Column(nullable = false, length = 50)
    private String category;

    private String description;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}