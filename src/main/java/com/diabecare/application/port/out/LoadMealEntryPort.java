package com.diabecare.application.port.out;

import com.diabecare.domain.model.MealEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LoadMealEntryPort {
    Optional<MealEntry> findById(UUID mealId);
    List<MealEntry> findByPatientIdAndDate(UUID patientId, LocalDate date);
    // Rango completo, sin paginar: usado por resúmenes/estadísticas/exportación.
    List<MealEntry> findByPatientIdAndDateRange(UUID patientId,
                                                LocalDate from,
                                                LocalDate to);
    Page<MealEntry> findByPatientIdAndDateRange(UUID patientId,
                                                LocalDate from,
                                                LocalDate to,
                                                Pageable pageable);
    void deleteById(UUID mealId);

    // Cursor de sincronización incremental para el móvil offline-first — ver
    // MealEntryJpaRepository.findByPatientIdAndUpdatedAtAfterOrderByUpdatedAtAsc.
    List<MealEntry> findByPatientIdUpdatedAfter(UUID patientId, LocalDateTime since);
}