ALTER TABLE patients
    ADD COLUMN IF NOT EXISTS insulin_sensitivity_factor NUMERIC(6,2),
    ADD COLUMN IF NOT EXISTS insulin_to_carb_ratio      NUMERIC(6,2),
    ADD COLUMN IF NOT EXISTS target_glucose_correction  NUMERIC(6,2);