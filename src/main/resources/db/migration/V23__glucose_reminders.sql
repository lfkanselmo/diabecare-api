-- Fase 5b: recordatorios proactivos de medición de glucosa, en horarios que el
-- propio paciente configura (a diferencia de los recordatorios de medicamento,
-- que se derivan automáticamente de la frecuencia ya registrada).

CREATE TABLE glucose_reminders (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id     UUID NOT NULL REFERENCES patients(id) ON DELETE CASCADE,
    reminder_time  TIME NOT NULL,
    label          VARCHAR(50),
    enabled        BOOLEAN NOT NULL DEFAULT TRUE,
    created_at     TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_glucose_reminders_patient_id ON glucose_reminders(patient_id);
CREATE INDEX idx_glucose_reminders_time ON glucose_reminders(reminder_time) WHERE enabled;
