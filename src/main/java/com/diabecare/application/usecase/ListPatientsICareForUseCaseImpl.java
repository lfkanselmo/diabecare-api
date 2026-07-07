package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ListPatientsICareForUseCase;
import com.diabecare.application.port.out.LoadCaregiverLinkPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.domain.model.CaregiverLink;
import com.diabecare.domain.model.Patient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ListPatientsICareForUseCaseImpl implements ListPatientsICareForUseCase {

    private final LoadCaregiverLinkPort loadCaregiverLinkPort;
    private final LoadPatientPort       loadPatientPort;

    @Override
    public List<PatientAccessView> execute(UUID caregiverUserId) {
        return loadCaregiverLinkPort.findActiveByCaregiverUserId(caregiverUserId).stream()
                .map(this::toView)
                .toList();
    }

    private PatientAccessView toView(CaregiverLink link) {
        String name = loadPatientPort.findById(link.getPatientId())
                .map(Patient::getFullName)
                .orElse("");
        return new PatientAccessView(link.getPatientId(), name, link.getCreatedAt());
    }
}
