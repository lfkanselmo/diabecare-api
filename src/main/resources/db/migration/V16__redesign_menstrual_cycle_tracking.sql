ALTER TABLE menstrual_cycles DROP COLUMN IF EXISTS cycle_length_days;
ALTER TABLE menstrual_cycles DROP COLUMN IF EXISTS period_length_days;
ALTER TABLE menstrual_cycles DROP COLUMN IF EXISTS phase;
ALTER TABLE menstrual_cycles DROP COLUMN IF EXISTS symptoms;

ALTER TABLE menstrual_cycles RENAME COLUMN cycle_start_date TO start_date;
ALTER TABLE menstrual_cycles RENAME COLUMN cycle_end_date TO end_date;

CREATE TABLE cycle_day_entries (
                                   id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                   cycle_id UUID NOT NULL REFERENCES menstrual_cycles(id) ON DELETE CASCADE,
                                   patient_id UUID NOT NULL REFERENCES patients(id),
                                   entry_date DATE NOT NULL,
                                   flow_intensity VARCHAR(20) NOT NULL DEFAULT 'NONE',
                                   notes VARCHAR(500),
                                   created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                   UNIQUE (cycle_id, entry_date)
);

CREATE INDEX idx_cycle_day_entries_patient_date ON cycle_day_entries(patient_id, entry_date);
CREATE INDEX idx_cycle_day_entries_cycle ON cycle_day_entries(cycle_id);

CREATE TABLE cycle_day_symptoms (
                                    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    day_entry_id UUID NOT NULL REFERENCES cycle_day_entries(id) ON DELETE CASCADE,
                                    symptom_code VARCHAR(30) NOT NULL,
                                    severity VARCHAR(10) NOT NULL,
                                    UNIQUE (day_entry_id, symptom_code)
);

CREATE INDEX idx_cycle_day_symptoms_day_entry ON cycle_day_symptoms(day_entry_id);