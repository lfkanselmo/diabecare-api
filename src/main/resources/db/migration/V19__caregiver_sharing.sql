-- Fase 3: compartir con cuidadores (acceso de solo lectura vía código de invitación).
-- caregiver_invites: código opaco de un solo uso que el paciente genera y comparte
-- por el canal que prefiera (no hay envío de correo en este sistema, ver RefreshToken
-- para el mismo patrón de secreto+hash).
-- caregiver_links: la relación de acceso ya activa entre un paciente y su cuidador.

CREATE TABLE caregiver_invites (
    id                   UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id           UUID NOT NULL REFERENCES patients(id),
    code_hash            VARCHAR(255) NOT NULL UNIQUE,
    expires_at           TIMESTAMP NOT NULL,
    redeemed_at          TIMESTAMP,
    redeemed_by_user_id  UUID REFERENCES users(id),
    revoked_at           TIMESTAMP,
    created_at           TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_caregiver_invites_patient_id ON caregiver_invites(patient_id);
CREATE INDEX idx_caregiver_invites_code_hash ON caregiver_invites(code_hash);

CREATE TABLE caregiver_links (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id         UUID NOT NULL REFERENCES patients(id),
    caregiver_user_id  UUID NOT NULL REFERENCES users(id),
    status             VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at         TIMESTAMP NOT NULL DEFAULT NOW(),
    revoked_at         TIMESTAMP,
    UNIQUE (patient_id, caregiver_user_id)
);

CREATE INDEX idx_caregiver_links_patient_id ON caregiver_links(patient_id);
CREATE INDEX idx_caregiver_links_caregiver_user_id ON caregiver_links(caregiver_user_id);
