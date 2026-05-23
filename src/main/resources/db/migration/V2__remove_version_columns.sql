ALTER TABLE users           DROP COLUMN version;
ALTER TABLE patients        DROP COLUMN version;
ALTER TABLE glucose_readings DROP COLUMN version;
ALTER TABLE meal_entries    DROP COLUMN version;
ALTER TABLE vital_signs     DROP COLUMN version;
ALTER TABLE medications     DROP COLUMN version;