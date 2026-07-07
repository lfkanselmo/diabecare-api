package com.diabecare.application.usecase;

import com.diabecare.application.port.in.RedeemCaregiverInviteUseCase;
import com.diabecare.application.port.out.CaregiverInvitePort;
import com.diabecare.application.port.out.LoadCaregiverLinkPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.SaveAuditLogPort;
import com.diabecare.application.port.out.SaveCaregiverLinkPort;
import com.diabecare.domain.exception.InvalidCaregiverInviteException;
import com.diabecare.domain.exception.PatientNotFoundException;
import com.diabecare.domain.model.CaregiverInvite;
import com.diabecare.domain.model.CaregiverLink;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class RedeemCaregiverInviteUseCaseImpl implements RedeemCaregiverInviteUseCase {

    private static final String INVITE_INVALID_MESSAGE =
            "El código de invitación es inválido, ya fue usado o expiró.";

    private final CaregiverInvitePort   caregiverInvitePort;
    private final LoadPatientPort       loadPatientPort;
    private final LoadCaregiverLinkPort loadCaregiverLinkPort;
    private final SaveCaregiverLinkPort saveCaregiverLinkPort;
    private final SaveAuditLogPort      saveAuditLogPort;
    private final AuditService          auditService;

    @Override
    public Result execute(Command command) {
        CaregiverInvite invite = caregiverInvitePort.findByRawCode(command.code())
                .filter(CaregiverInvite::isValid)
                .orElseThrow(() -> new InvalidCaregiverInviteException(INVITE_INVALID_MESSAGE));

        Patient patient = loadPatientPort.findById(invite.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(invite.getPatientId().toString()));

        if (patient.getUserId().equals(command.caregiverUserId())) {
            throw new InvalidCaregiverInviteException("No puedes ser cuidador de ti mismo.");
        }

        if (loadCaregiverLinkPort.existsActive(patient.getPatientId(), command.caregiverUserId())) {
            throw new InvalidCaregiverInviteException("Ya tienes acceso a los datos de este paciente.");
        }

        caregiverInvitePort.markRedeemed(invite.getId(), command.caregiverUserId());

        CaregiverLink saved = saveCaregiverLinkPort.save(
                CaregiverLink.create(patient.getPatientId(), command.caregiverUserId()));

        saveAuditLogPort.save(auditService.buildCreateLog(
                patient.getPatientId(), "CAREGIVER_LINK", saved.getId()));

        return new Result(patient.getPatientId(), patient.getFullName());
    }
}
