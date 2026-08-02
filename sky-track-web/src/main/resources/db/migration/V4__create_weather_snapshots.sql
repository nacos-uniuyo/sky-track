CREATE TABLE weather_snapshots (
    id BIGSERIAL PRIMARY KEY,
    tracked_city_id BIGINT NOT NULL,
    temperature DECIMAL(5,2),
    feels_like DECIMAL(5,2),
    humidity INTEGER,
    wind_speed DECIMAL(5,2),
    wind_direction INTEGER,
    pressure INTEGER,
    visibility INTEGER,
    condition_code INTEGER,
    condition_text VARCHAR(120),
    sunrise TIMESTAMPTZ,
    sunset TIMESTAMPTZ,
    recorded_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT fk_snapshots_city FOREIGN KEY (tracked_city_id) REFERENCES tracked_cities(id) ON DELETE CASCADE
);

CREATE INDEX idx_snapshots_city_time ON weather_snapshots (tracked_city_id, recorded_at);
