-- Preparacion para push nativo (FCM) desde la futura app movil (Android/iOS),
-- paralelo a push_subscriptions (Web Push). PushNotificationService enviara por
-- ambos canales segun que suscripciones tenga cada paciente.

CREATE TABLE mobile_push_tokens (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    patient_id   UUID NOT NULL,
    device_token TEXT NOT NULL UNIQUE,
    platform     VARCHAR(10) NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_mobile_push_patient FOREIGN KEY (patient_id)
        REFERENCES patients(id) ON DELETE CASCADE
);

CREATE INDEX idx_mobile_push_tokens_patient ON mobile_push_tokens(patient_id);
