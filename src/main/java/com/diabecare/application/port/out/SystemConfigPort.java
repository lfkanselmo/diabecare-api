package com.diabecare.application.port.out;

import com.diabecare.domain.model.SystemConfig;
import java.util.List;

public interface SystemConfigPort {
    int     getInt(String key);
    double  getDecimal(String key);
    String  getString(String key);
    List<SystemConfig> findAll();
    void    reload();
}