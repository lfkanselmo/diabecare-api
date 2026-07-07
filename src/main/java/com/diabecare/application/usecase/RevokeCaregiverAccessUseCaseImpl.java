package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RevokeCaregiverAccessUseCase;
import com.diabecare.application.port.out.LoadCaregiverLinkPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SaveCaregiverLinkPort;
import com.diabecare.domain.exception.UnauthorizedResourceAccessException;
import com.diabecare.domain.model.CaregiverLink;
import com.diabecare.domain.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RevokeCaregiverAccessUseCaseImpl implements RevokeCaregiverAccessUseCase {

    private static final String NOT_OWNER_MESSAGE =
            "No tienes permiso para modificar este acceso de cuidador";

    private final LoadCaregiverLinkPort loadCaregiverLinkPort;
    private final SaveCaregiverLinkPort saveCaregiverLinkPort;
    private final SaveAuditLogPort      saveAuditLogPort;
    private final AuditService          auditService;

    @Override
    public void execute(Command command) {
        CaregiverLink link = loadCaregiverLinkPort.findById(command.linkId())
                .orElseThrow(() -> new UnauthorizedResourceAccessException(NOT_OWNER_MESSAGE));

        if (!link.getPatientId().equals(command.patientId())) {
            throw new UnauthorizedResourceAccessException(NOT_OWNER_MESSAGE);
        }

        saveAuditLogPort.save(auditService.buildDeleteLog(
                command.patientId(), "CAREGIVER_LINK", link.getId()));

        link.revoke();
        saveCaregiverLinkPort.save(link);
    }
}
