package com.diabecare.application.usecase;

import com.diabecare.application.port.in.ListMyCaregiversUseCase;
import com.diabecare.application.port.out.LoadCaregiverLinkPort;
import com.diabecare.application.port.out.LoadPatientPort;
import com.diabecare.application.port.out.LoadUserPort;
import com.diabecare.domain.model.CaregiverLink;
import com.diabecare.domain.model.Patient;
import com.diabecare.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ListMyCaregiversUseCaseImpl implements ListMyCaregiversUseCase {

    private final LoadCaregiverLinkPort loadCaregiverLinkPort;
    private final LoadPatientPort       loadPatientPort;
    private final LoadUserPort          loadUserPort;

    @Override
    public List<CaregiverView> execute(UUID patientId) {
        return loadCaregiverLinkPort.findActiveByPatientId(patientId).stream()
                .map(this::toView)
                .toList();
    }

    private CaregiverView toView(CaregiverLink link) {
        String name = loadPatientPort.findByUserId(link.getCaregiverUserId())
                .map(Patient::getFullName)
                .orElse("");
        String email = loadUserPort.findById(link.getCaregiverUserId())
                .map(User::getEmail)
                .orElse("");
        return new CaregiverView(
                link.getId(), link.getCaregiverUserId(), name, email, link.getCreatedAt());
    }
}
