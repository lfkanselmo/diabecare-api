DROP TABLE IF EXISTS foods;

CREATE TABLE foods (
                       id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                       name VARCHAR(200) NOT NULL,
                       category VARCHAR(50) NOT NULL,
                       calories_per_100g NUMERIC(8,2) NOT NULL,
                       carbs_per_100g NUMERIC(8,2) NOT NULL,
                       proteins_per_100g NUMERIC(8,2) NOT NULL,
                       fats_per_100g NUMERIC(8,2) NOT NULL,
                       fiber_per_100g NUMERIC(8,2),
                       sodium_per_100g NUMERIC(8,2),
                       created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_foods_name ON foods(name);
CREATE INDEX idx_foods_category ON foods(category);