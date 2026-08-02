CREATE TABLE weather_alerts (
    id BIGSERIAL PRIMARY KEY,
    tracked_city_id BIGINT NOT NULL,
    alert_type VARCHAR(50) NOT NULL,
    severity VARCHAR(20) NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    detected_at TIMESTAMPTZ NOT NULL,
    acknowledged BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_alerts_city FOREIGN KEY (tracked_city_id) REFERENCES tracked_cities(id) ON DELETE CASCADE
);

CREATE INDEX idx_alerts_city_time ON weather_alerts (tracked_city_id, detected_at);
