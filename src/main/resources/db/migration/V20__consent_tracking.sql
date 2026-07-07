-- Fase 4 (Ley 1581 de 2012 - Habeas Data): registro de la aceptación de la
-- política de tratamiento de datos personales. Nullable porque los usuarios
-- ya existentes nunca la aceptaron bajo este mecanismo (quedan sin marcar,
-- no se les fuerza retroactivamente).
ALTER TABLE users ADD COLUMN terms_accepted_at TIMESTAMP;
ALTER TABLE users ADD COLUMN terms_version VARCHAR(20);
