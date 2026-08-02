CREATE TABLE corridor_routes (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    origin_city_id BIGINT,
    destination_city_id BIGINT,
    waypoint_count INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_corridor_routes_user FOREIGN KEY (user_id) REFERENCES app_users(id) ON DELETE CASCADE,
    CONSTRAINT fk_corridor_routes_origin FOREIGN KEY (origin_city_id) REFERENCES tracked_cities(id) ON DELETE SET NULL,
    CONSTRAINT fk_corridor_routes_dest FOREIGN KEY (destination_city_id) REFERENCES tracked_cities(id) ON DELETE SET NULL
);

CREATE INDEX idx_corridor_routes_user ON corridor_routes (user_id);
