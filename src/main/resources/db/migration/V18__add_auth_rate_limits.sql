INSERT INTO system_config (key, value, data_type, category, description) VALUES
('rate_limit.login_per_hour',    '10', 'INTEGER', 'RATE_LIMIT', 'Máx intentos de login por hora por dirección IP'),
('rate_limit.register_per_hour', '5',  'INTEGER', 'RATE_LIMIT', 'Máx registros de cuenta por hora por dirección IP');
