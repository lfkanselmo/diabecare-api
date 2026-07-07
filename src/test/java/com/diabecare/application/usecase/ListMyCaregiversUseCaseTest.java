package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ListMyCaregiversUseCase;
import com.diabecare.application.port.out.LoadCaregiverLinkPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("ListMyCaregiversUseCaseImpl")
class ListMyCaregiversUseCaseTest {

    @Mock
    private LoadCaregiverLinkPort loadCaregiverLinkPort;
    @Mock
    private LoadPatientPort loadPatientPort;
    @Mock
    private LoadUserPort loadUserPort;

    private ListMyCaregiversUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();
    private final UUID caregiverUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ListMyCaregiversUseCaseImpl(loadCaregiverLinkPort, loadPatientPort, loadUserPort);
    }

    @Test
    @DisplayName("retorna los cuidadores activos con su nombre y correo resueltos")
    void returnsActiveCaregiversWithNameAndEmailResolved() {
        CaregiverLink link = CaregiverLink.create(patientId, caregiverUserId);
        Patient caregiverAsPatient = Patient.builder()
                .patientId(UUID.randomUUID())
                .userId(caregiverUserId)
                .fullName("Carlos Pérez")
                .build();
        User caregiverUser = User.builder().id(caregiverUserId).email("carlos@example.com").build();

        when(loadCaregiverLinkPort.findActiveByPatientId(patientId)).thenReturn(List.of(link));
        when(loadPatientPort.findByUserId(caregiverUserId)).thenReturn(Optional.of(caregiverAsPatient));
        when(loadUserPort.findById(caregiverUserId)).thenReturn(Optional.of(caregiverUser));

        List<ListMyCaregiversUseCase.CaregiverView> result = useCase.execute(patientId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).caregiverName()).isEqualTo("Carlos Pérez");
        assertThat(result.get(0).caregiverEmail()).isEqualTo("carlos@example.com");
        assertThat(result.get(0).linkId()).isEqualTo(link.getId());
    }

    @Test
    @DisplayName("retorna nombre y correo vacíos cuando no se pueden resolver")
    void returnsEmptyNameAndEmailWhenCannotBeResolved() {
        CaregiverLink link = CaregiverLink.create(patientId, caregiverUserId);

        when(loadCaregiverLinkPort.findActiveByPatientId(patientId)).thenReturn(List.of(link));
        when(loadPatientPort.findByUserId(caregiverUserId)).thenReturn(Optional.empty());
        when(loadUserPort.findById(caregiverUserId)).thenReturn(Optional.empty());

        List<ListMyCaregiversUseCase.CaregiverView> result = useCase.execute(patientId);

        assertThat(result.get(0).caregiverName()).isEmpty();
        assertThat(result.get(0).caregiverEmail()).isEmpty();
    }

    @Test
    @DisplayName("retorna una lista vacía cuando no hay cuidadores activos")
    void returnsEmptyListWhenNoActiveCaregivers() {
        when(loadCaregiverLinkPort.findActiveByPatientId(patientId)).thenReturn(List.of());

        assertThat(useCase.execute(patientId)).isEmpty();
    }
}
