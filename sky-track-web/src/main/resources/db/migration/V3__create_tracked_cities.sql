CREATE TABLE tracked_cities (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    city_name VARCHAR(120) NOT NULL,
    country VARCHAR(120),
    country_code VARCHAR(2),
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_tracked_cities_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE
);

CREATE INDEX idx_tracked_cities_user ON tracked_cities (user_id);
