package com.diabecare.domain.service;

import com.diabecare.domain.model.AccountExportData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

public class AccountExportService {

    private final ObjectMapper objectMapper;

    public AccountExportService() {
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .enable(SerializationFeature.INDENT_OUTPUT);
    }

    public String toJson(AccountExportData data) {
        try {
            return objectMapper.writeValueAsString(data);
        } catch (Exception e) {
            throw new RuntimeException("Error serializando la exportación de la cuenta a JSON", e);
        }
    }
}
