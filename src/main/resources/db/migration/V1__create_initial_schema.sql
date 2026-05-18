CREATE TABLE users (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       email VARCHAR(150) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(30) NOT NULL,
                       enabled BOOLEAN NOT NULL DEFAULT TRUE,
                       created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                       version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE patients (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          user_id UUID NOT NULL UNIQUE REFERENCES users(id),
                          full_name VARCHAR(150) NOT NULL,
                          date_of_birth DATE NOT NULL,
                          diabetes_type VARCHAR(30) NOT NULL,
                          diagnosis_date DATE NOT NULL,
                          height_cm NUMERIC(5,2) NOT NULL,
                          target_glucose_min NUMERIC(6,2) NOT NULL DEFAULT 70,
                          target_glucose_max NUMERIC(6,2) NOT NULL DEFAULT 180,
                          daily_calorie_goal INTEGER,
                          activity_level VARCHAR(30) NOT NULL DEFAULT 'SEDENTARY',
                          preferred_glucose_unit VARCHAR(10) NOT NULL DEFAULT 'MG_DL',
                          created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                          version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE glucose_readings (
                                  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                  patient_id UUID NOT NULL REFERENCES patients(id),
                                  value NUMERIC(6,2) NOT NULL,
                                  unit VARCHAR(10) NOT NULL,
                                  reading_type VARCHAR(20) NOT NULL,
                                  measured_at TIMESTAMP NOT NULL,
                                  notes VARCHAR(500),
                                  device_source VARCHAR(100),
                                  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                                  version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE meal_entries (
                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                              patient_id UUID NOT NULL REFERENCES patients(id),
                              meal_type VARCHAR(20) NOT NULL,
                              consumed_at TIMESTAMP NOT NULL,
                              notes VARCHAR(500),
                              created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                              version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE meal_items (
                            id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                            meal_entry_id UUID NOT NULL REFERENCES meal_entries(id) ON DELETE CASCADE,
                            food_name VARCHAR(200) NOT NULL,
                            quantity_grams NUMERIC(8,2) NOT NULL,
                            calories NUMERIC(8,2) NOT NULL,
                            carbohydrates NUMERIC(8,2) NOT NULL,
                            proteins NUMERIC(8,2),
                            fats NUMERIC(8,2),
                            food_code VARCHAR(50)
);

CREATE TABLE vital_signs (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             patient_id UUID NOT NULL REFERENCES patients(id),
                             weight_kg NUMERIC(6,2),
                             height_cm NUMERIC(5,2),
                             systolic_bp INTEGER,
                             diastolic_bp INTEGER,
                             heart_rate INTEGER,
                             hba1c NUMERIC(5,2),
                             measured_at TIMESTAMP NOT NULL,
                             notes VARCHAR(500),
                             created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                             version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE medications (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             patient_id UUID NOT NULL REFERENCES patients(id),
                             name VARCHAR(150) NOT NULL,
                             type VARCHAR(30) NOT NULL,
                             dose NUMERIC(8,2) NOT NULL,
                             dose_unit VARCHAR(20) NOT NULL,
                             frequency VARCHAR(30) NOT NULL,
                             start_date DATE NOT NULL,
                             end_date DATE,
                             active BOOLEAN NOT NULL DEFAULT TRUE,
                             notes VARCHAR(500),
                             created_at TIMESTAMP NOT NULL DEFAULT NOW(),
                             updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
                             version BIGINT NOT NULL DEFAULT 0
);

-- Índices de rendimiento
CREATE INDEX idx_glucose_readings_patient_date
    ON glucose_readings(patient_id, measured_at DESC);

CREATE INDEX idx_meal_entries_patient_date
    ON meal_entries(patient_id, consumed_at DESC);

CREATE INDEX idx_vital_signs_patient_date
    ON vital_signs(patient_id, measured_at DESC);

CREATE INDEX idx_medications_patient_active
    ON medications(patient_id, active);