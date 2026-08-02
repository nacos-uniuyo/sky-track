package com.skywatch.weather.repository;

import com.skywatch.weather.model.WeatherAlert;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {

    List<WeatherAlert> findByTrackedCityIdOrderByDetectedAtDesc(Long trackedCityId);

    List<WeatherAlert> findAllByTrackedCityUserIdOrderByDetectedAtDesc(Long userId);

    boolean existsByIdAndTrackedCityUserId(Long id, Long userId);
}
