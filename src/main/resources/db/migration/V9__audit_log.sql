CREATE TABLE audit_log (
                           id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                           patient_id    UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
                           entity_type   VARCHAR(50)  NOT NULL,
                           entity_id     UUID,
                           action        VARCHAR(20)  NOT NULL,
                           field_name    VARCHAR(100),
                           old_value     TEXT,
                           new_value     TEXT,
                           performed_at  TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_log_patient    ON audit_log(patient_id);
CREATE INDEX idx_audit_log_entity     ON audit_log(entity_type, entity_id);
CREATE INDEX idx_audit_log_performed  ON audit_log(performed_at DESC);