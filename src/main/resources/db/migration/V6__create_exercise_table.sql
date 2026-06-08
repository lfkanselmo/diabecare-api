CREATE TABLE exercise_logs (
                               id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                               patient_id UUID NOT NULL REFERENCES patients(id),
                               exercise_type VARCHAR(50) NOT NULL,
                               intensity VARCHAR(20) NOT NULL,
                               duration_minutes INTEGER NOT NULL,
                               calories_burned NUMERIC(8,2),
                               notes VARCHAR(500),
                               performed_at TIMESTAMP NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                               updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_exercise_logs_patient_date
    ON exercise_logs(patient_id, performed_at DESC);