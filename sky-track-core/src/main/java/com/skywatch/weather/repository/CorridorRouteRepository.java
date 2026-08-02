package com.skywatch.weather.repository;

import com.skywatch.weather.model.CorridorRoute;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CorridorRouteRepository extends JpaRepository<CorridorRoute, Long> {

    List<CorridorRoute> findByUserIdOrderByCreatedAtDesc(Long userId);
}
