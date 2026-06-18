package com.diabecare.infrastructure.persistence.adapter;

import com.diabecare.application.port.out.SystemConfigPort;
import com.diabecare.domain.model.SystemConfig;
import com.diabecare.infrastructure.persistence.entity.SystemConfigEntity;
import com.diabecare.infrastructure.persistence.repository.SystemConfigJpaRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class SystemConfigAdapter implements SystemConfigPort {

    private final SystemConfigJpaRepository repository;
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        reload();
    }

    @Override
    public int getInt(String key) {
        return Integer.parseInt(getValue(key));
    }

    @Override
    public double getDecimal(String key) {
        return Double.parseDouble(getValue(key));
    }

    @Override
    public String getString(String key) {
        return getValue(key);
    }

    @Override
    public List<SystemConfig> findAll() {
        return repository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public void reload() {
        cache.clear();
        repository.findAll().forEach(e -> cache.put(e.getKey(), e.getValue()));
        log.info("SystemConfig recargado — {} parámetros en caché", cache.size());
    }

    private String getValue(String key) {
        String value = cache.get(key);
        if (value == null) throw new IllegalArgumentException("Config no encontrada: " + key);
        return value;
    }

    private SystemConfig toDomain(SystemConfigEntity entity) {
        return SystemConfig.builder()
                .key(entity.getKey())
                .value(entity.getValue())
                .dataType(SystemConfig.DataType.valueOf(entity.getDataType()))
                .category(SystemConfig.Category.valueOf(entity.getCategory()))
                .description(entity.getDescription())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}