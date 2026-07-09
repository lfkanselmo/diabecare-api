package com.diabecare.application.port.out;

import com.diabecare.domain.model.ExternalFoodInfo;

import java.util.Optional;

public interface FoodLookupPort {
    Optional<ExternalFoodInfo> findByBarcode(String barcode);
}
