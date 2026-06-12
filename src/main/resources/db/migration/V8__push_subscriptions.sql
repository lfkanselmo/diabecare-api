CREATE TABLE push_subscriptions (
                                    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                                    patient_id  UUID NOT NULL,
                                    endpoint    TEXT NOT NULL UNIQUE,
                                    p256dh      VARCHAR(500) NOT NULL,
                                    auth        VARCHAR(200) NOT NULL,
                                    created_at  TIMESTAMP NOT NULL DEFAULT NOW(),
                                    CONSTRAINT fk_push_patient FOREIGN KEY (patient_id)
                                        REFERENCES patients(id) ON DELETE CASCADE
);

CREATE INDEX idx_push_subscriptions_patient ON push_subscriptions(patient_id);