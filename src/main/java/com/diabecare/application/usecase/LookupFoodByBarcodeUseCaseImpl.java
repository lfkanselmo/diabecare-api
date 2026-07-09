package com.diabecare.application.usecase;

import com.diabecare.application.port.in.LookupFoodByBarcodeUseCase;
import com.diabecare.application.port.out.FoodLookupPort;
import com.diabecare.domain.model.ExternalFoodInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LookupFoodByBarcodeUseCaseImpl implements LookupFoodByBarcodeUseCase {

    private final FoodLookupPort foodLookupPort;

    @Override
    public Optional<ExternalFoodInfo> execute(String barcode) {
        String normalized = barcode.trim();
        if (normalized.isEmpty()) {
            return Optional.empty();
        }

        return foodLookupPort.findByBarcode(normalized);
    }
}
