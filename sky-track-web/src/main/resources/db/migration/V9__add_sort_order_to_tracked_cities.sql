ALTER TABLE tracked_cities ADD COLUMN sort_order INTEGER NOT NULL DEFAULT 0;

UPDATE tracked_cities SET sort_order = id;

CREATE INDEX idx_tracked_cities_user_order ON tracked_cities (user_id, sort_order);
