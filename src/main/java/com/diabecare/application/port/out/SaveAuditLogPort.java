package com.diabecare.application.port.out;

import com.diabecare.domain.model.AuditLog;

public interface SaveAuditLogPort {
    void save(AuditLog auditLog);
}