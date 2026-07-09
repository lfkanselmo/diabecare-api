-- Preparacion para integraciones de dispositivos de medicion de glucosa (CGM/glucometros)
-- sin usuarios reales todavia: una API key opaca por paciente, revocable, que un futuro
-- bridge externo (Nightscout, un glucometro con su propia app de sincronizacion, etc.)
-- puede usar para importar lecturas sin necesitar un login interactivo (JWT). Mismo
-- patron de secreto+hash que caregiver_invites/refresh_tokens.

CREATE TABLE device_api_keys (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id    UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    label         VARCHAR(100) NOT NULL,
    key_hash      VARCHAR(255) NOT NULL UNIQUE,
    created_at    TIMESTAMP NOT NULL DEFAULT NOW(),
    last_used_at  TIMESTAMP,
    revoked_at    TIMESTAMP
);

CREATE INDEX idx_device_api_keys_patient_id ON device_api_keys(patient_id);
CREATE INDEX idx_device_api_keys_key_hash ON device_api_keys(key_hash);

-- Limite generoso (cubre un dia completo de un CGM cada 5 min + margen de backfill)
-- distinto del limite de registro manual (rate_limit.glucose_per_hour = 20), que
-- asfixiaria una importacion legitima de un sensor continuo.
INSERT INTO system_config (key, value, data_type, category, description) VALUES
('rate_limit.device_import_per_hour', '300', 'INTEGER', 'RATE_LIMIT', 'Máx lecturas de glucosa importadas por hora por API key de dispositivo');
