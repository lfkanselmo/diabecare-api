package com.diabecare.application.usecase;

import com.diabecare.application.port.in.GetSystemConfigUseCase;
import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.model.SystemConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetSystemConfigUseCaseImpl implements GetSystemConfigUseCase {

    private final SystemConfigPort systemConfigPort;

    @Override
    public List<SystemConfig> getAll() {
        return systemConfigPort.findAll();
    }

    @Override
    public void reload() {
        systemConfigPort.reload();
    }
}