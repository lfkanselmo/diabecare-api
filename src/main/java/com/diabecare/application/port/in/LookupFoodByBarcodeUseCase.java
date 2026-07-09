package com.diabecare.application.port.in;

import com.diabecare.domain.model.ExternalFoodInfo;

import java.util.Optional;

public interface LookupFoodByBarcodeUseCase {
    Optional<ExternalFoodInfo> execute(String barcode);
}
