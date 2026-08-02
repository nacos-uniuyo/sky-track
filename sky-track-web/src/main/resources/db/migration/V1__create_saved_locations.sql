CREATE TABLE saved_locations (
    id BIGSERIAL PRIMARY KEY,
    city VARCHAR(120) NOT NULL,
    country_code VARCHAR(2),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_saved_locations_city ON saved_locations (city);
