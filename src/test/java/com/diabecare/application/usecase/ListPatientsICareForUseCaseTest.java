package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ListPatientsICareForUseCase;
import com.diabecare.application.port.out.LoadCaregiverLinkPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.model.CaregiverLink;
import com.diabecare.domain.model.Patient;
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
@DisplayName("ListPatientsICareForUseCaseImpl")
class ListPatientsICareForUseCaseTest {

    @Mock
    private LoadCaregiverLinkPort loadCaregiverLinkPort;
    @Mock
    private LoadPatientPort loadPatientPort;

    private ListPatientsICareForUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();
    private final UUID caregiverUserId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new ListPatientsICareForUseCaseImpl(loadCaregiverLinkPort, loadPatientPort);
    }

    @Test
    @DisplayName("retorna los pacientes activos con su nombre resuelto")
    void returnsActivePatientsWithNameResolved() {
        CaregiverLink link = CaregiverLink.create(patientId, caregiverUserId);
        Patient patient = Patient.builder().patientId(patientId).fullName("Ana García").build();

        when(loadCaregiverLinkPort.findActiveByCaregiverUserId(caregiverUserId)).thenReturn(List.of(link));
        when(loadPatientPort.findById(patientId)).thenReturn(Optional.of(patient));

        List<ListPatientsICareForUseCase.PatientAccessView> result = useCase.execute(caregiverUserId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).patientId()).isEqualTo(patientId);
        assertThat(result.get(0).patientFullName()).isEqualTo("Ana García");
    }

    @Test
    @DisplayName("retorna una lista vacía cuando no cuida a ningún paciente")
    void returnsEmptyListWhenNotCaringForAnyPatient() {
        when(loadCaregiverLinkPort.findActiveByCaregiverUserId(caregiverUserId)).thenReturn(List.of());

        assertThat(useCase.execute(caregiverUserId)).isEmpty();
    }
}
