package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.SystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SystemConfigJpaRepository extends JpaRepository<SystemConfigEntity, String> {
    List<SystemConfigEntity> findByCategory(String category);
}