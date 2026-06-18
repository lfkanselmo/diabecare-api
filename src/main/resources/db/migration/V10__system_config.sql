CREATE TABLE system_config (
                               key         VARCHAR(100) PRIMARY KEY,
                               value       TEXT        NOT NULL,
                               data_type   VARCHAR(20) NOT NULL,
                               category    VARCHAR(50) NOT NULL,
                               description TEXT,
                               updated_at  TIMESTAMP   NOT NULL DEFAULT NOW()
);

INSERT INTO system_config (key, value, data_type, category, description) VALUES
-- Alertas
('alert.hours_without_glucose',      '8',    'INTEGER', 'ALERTS',    'Horas sin glucosa para generar alerta'),
('alert.hba1c_threshold',            '8.0',  'DECIMAL', 'ALERTS',    'HbA1c estimada que dispara alerta'),
('alert.good_tir_threshold',         '70.0', 'DECIMAL', 'ALERTS',    'TIR mínimo para alerta de racha positiva'),
('alert.streak_days',                '7',    'INTEGER', 'ALERTS',    'Días para evaluar racha positiva'),
('alert.min_readings_for_stats',     '3',    'INTEGER', 'ALERTS',    'Mínimo lecturas para calcular estadísticas'),
-- Patrones
('pattern.days_window',              '14',   'INTEGER', 'PATTERNS',  'Ventana de días para detección de patrones'),
('pattern.fasting_threshold_mgdl',   '130',  'INTEGER', 'PATTERNS',  'Umbral glucosa en ayuno (mg/dL)'),
('pattern.postmeal_threshold_mgdl',  '180',  'INTEGER', 'PATTERNS',  'Umbral glucosa postprandial (mg/dL)'),
('pattern.fasting_ratio',            '0.6',  'DECIMAL', 'PATTERNS',  'Ratio mínimo de ayunos altos para detectar patrón'),
('pattern.postmeal_ratio',           '0.5',  'DECIMAL', 'PATTERNS',  'Ratio mínimo de postprandiales altos para detectar patrón'),
('pattern.hypo_min_episodes',        '3',    'INTEGER', 'PATTERNS',  'Mínimo episodios de hipoglucemia para detectar patrón'),
('pattern.cv_threshold',             '36.0', 'DECIMAL', 'PATTERNS',  'Umbral CV para detectar alta variabilidad (%)'),
('pattern.min_readings_variability', '7',    'INTEGER', 'PATTERNS',  'Mínimo lecturas para calcular variabilidad'),
-- Rate limiting
('rate_limit.glucose_per_hour',      '20',   'INTEGER', 'RATE_LIMIT','Máx registros de glucosa por hora por paciente'),
('rate_limit.meal_per_hour',         '15',   'INTEGER', 'RATE_LIMIT','Máx registros de comidas por hora por paciente'),
('rate_limit.exercise_per_hour',     '10',   'INTEGER', 'RATE_LIMIT','Máx registros de ejercicio por hora por paciente');