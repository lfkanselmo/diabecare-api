package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RevokeCaregiverAccessUseCase;
import com.diabecare.application.port.out.LoadCaregiverLinkPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SaveCaregiverLinkPort;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import com.diabecare.domain.model.CaregiverLink;
import com.diabecare.domain.service.AuditService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RevokeCaregiverAccessUseCaseImpl")
class RevokeCaregiverAccessUseCaseTest {

    @Mock
    private LoadCaregiverLinkPort loadCaregiverLinkPort;
    @Mock
    private SaveCaregiverLinkPort saveCaregiverLinkPort;
    @Mock
    private SaveAuditLogPort saveAuditLogPort;

    private RevokeCaregiverAccessUseCaseImpl useCase;
    private final UUID patientId = UUID.randomUUID();
    private final UUID linkId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        useCase = new RevokeCaregiverAccessUseCaseImpl(
                loadCaregiverLinkPort, saveCaregiverLinkPort, saveAuditLogPort, new AuditService());
    }

    @Test
    @DisplayName("revoca el enlace cuando pertenece al paciente autenticado")
    void revokesLinkWhenItBelongsToAuthenticatedPatient() {
        CaregiverLink link = CaregiverLink.create(patientId, UUID.randomUUID());
        when(loadCaregiverLinkPort.findById(linkId)).thenReturn(Optional.of(link));
        when(saveCaregiverLinkPort.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute(new RevokeCaregiverAccessUseCase.Command(patientId, linkId));

        assertThat(link.isActive()).isFalse();

        ArgumentCaptor<CaregiverLink> captor = ArgumentCaptor.forClass(CaregiverLink.class);
        verify(saveCaregiverLinkPort).save(captor.capture());
        assertThat(captor.getValue().isActive()).isFalse();
        verify(saveAuditLogPort).save(any());
    }

    @Test
    @DisplayName("lanza UnauthorizedResourceAccessException cuando el enlace no existe")
    void throwsWhenLinkDoesNotExist() {
        when(loadCaregiverLinkPort.findById(linkId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute(new RevokeCaregiverAccessUseCase.Command(patientId, linkId)))
                .isInstanceOf(UnauthorizedResourceAccessException.class);

        verifyNoInteractions(saveCaregiverLinkPort);
    }

    @Test
    @DisplayName("lanza UnauthorizedResourceAccessException cuando el enlace pertenece a otro paciente")
    void throwsWhenLinkBelongsToAnotherPatient() {
        CaregiverLink link = CaregiverLink.create(UUID.randomUUID(), UUID.randomUUID());
        when(loadCaregiverLinkPort.findById(linkId)).thenReturn(Optional.of(link));

        assertThatThrownBy(() -> useCase.execute(new RevokeCaregiverAccessUseCase.Command(patientId, linkId)))
                .isInstanceOf(UnauthorizedResourceAccessException.class);

        verifyNoInteractions(saveCaregiverLinkPort);
    }
}
