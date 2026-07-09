package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.domain.model.GlucoseReminder;
import com.diabecare.infrastructure.persistence.entity.GlucoseReminderEntity;
import com.diabecare.infrastructure.persistence.repository.GlucoseReminderJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GlucoseReminderPersistenceAdapter")
class GlucoseReminderPersistenceAdapterTest {

    @Mock
    private GlucoseReminderJpaRepository repository;

    private GlucoseReminderPersistenceAdapter adapter;
    private final UUID patientId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        adapter = new GlucoseReminderPersistenceAdapter(repository);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("persiste el recordatorio y retorna el dominio reconstruido")
        void persistsReminderAndReturnsReconstructedDomain() {
            GlucoseReminder reminder = GlucoseReminder.builder()
                    .patientId(patientId).reminderTime(LocalTime.of(7, 0))
                    .label("Ayunas").enabled(true).build();
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            GlucoseReminder result = adapter.save(reminder);

            assertThat(result.getPatientId()).isEqualTo(patientId);
            assertThat(result.getReminderTime()).isEqualTo(LocalTime.of(7, 0));

            ArgumentCaptor<GlucoseReminderEntity> captor = ArgumentCaptor.forClass(GlucoseReminderEntity.class);
            verify(repository).save(captor.capture());
            assertThat(captor.getValue().getCreatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findByPatientId")
    class FindByPatientId {

        @Test
        @DisplayName("retorna los recordatorios del paciente convertidos a dominio")
        void returnsPatientRemindersConvertedToDomain() {
            when(repository.findByPatientIdOrderByReminderTime(patientId))
                    .thenReturn(List.of(validEntity()));

            List<GlucoseReminder> result = adapter.findByPatientId(patientId);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findAllEnabledAtTime")
    class FindAllEnabledAtTime {

        @Test
        @DisplayName("retorna los recordatorios habilitados que coinciden con la hora dada")
        void returnsEnabledRemindersMatchingGivenTime() {
            when(repository.findByReminderTimeAndEnabledTrue(LocalTime.of(7, 0)))
                    .thenReturn(List.of(validEntity()));

            List<GlucoseReminder> result = adapter.findAllEnabledAtTime(LocalTime.of(7, 0));

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("delega el borrado al repositorio")
        void delegatesDeletionToRepository() {
            UUID reminderId = UUID.randomUUID();

            adapter.delete(reminderId);

            verify(repository).deleteById(reminderId);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("retorna Optional vacío cuando el recordatorio no existe")
        void returnsEmptyWhenReminderDoesNotExist() {
            UUID reminderId = UUID.randomUUID();
            when(repository.findById(reminderId)).thenReturn(Optional.empty());

            Optional<GlucoseReminder> result = adapter.findById(reminderId);

            assertThat(result).isEmpty();
        }
    }

    private GlucoseReminderEntity validEntity() {
        return GlucoseReminderEntity.builder()
                .id(UUID.randomUUID())
                .patientId(patientId)
                .reminderTime(LocalTime.of(7, 0))
                .label("Ayunas")
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
