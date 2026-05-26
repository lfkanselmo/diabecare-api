package com.diabecare.infrastructure.persistence.repository;

import com.diabecare.infrastructure.persistence.entity.FoodEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FoodJpaRepository extends JpaRepository<FoodEntity, UUID> {

    @Query("SELECT f FROM FoodEntity f WHERE LOWER(f.name) LIKE LOWER(CONCAT('%', :query, '%')) ORDER BY f.name")
    List<FoodEntity> searchByName(@Param("query") String query);

    List<FoodEntity> findByCategoryOrderByName(String category);
}