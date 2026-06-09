ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS biological_sex VARCHAR(15) DEFAULT 'NOT_SPECIFIED';

CREATE TABLE menstrual_cycles (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  patient_id UUID NOT NULL REFERENCES patients(id),
                                  cycle_start_date DATE NOT NULL,
                                  cycle_end_date DATE,
                                  cycle_length_days INTEGER,
                                  period_length_days INTEGER,
                                  phase VARCHAR(20) NOT NULL DEFAULT 'MENSTRUATION',
                                  symptoms TEXT,
                                  notes VARCHAR(500),
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_menstrual_cycles_patient
    ON menstrual_cycles(patient_id, cycle_start_date DESC);