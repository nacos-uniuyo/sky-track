package com.skywatch.weather.repository;

import com.skywatch.weather.model.WeatherSnapshot;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeatherSnapshotRepository extends JpaRepository<WeatherSnapshot, Long> {

    Optional<WeatherSnapshot> findFirstByTrackedCityIdOrderByRecordedAtDesc(Long trackedCityId);

    Optional<WeatherSnapshot> findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(
            Long trackedCityId, OffsetDateTime before);

    List<WeatherSnapshot> findByTrackedCityIdOrderByRecordedAtAsc(Long trackedCityId);

    List<WeatherSnapshot> findByTrackedCityIdAndRecordedAtBetweenOrderByRecordedAtAsc(
            Long trackedCityId, OffsetDateTime from, OffsetDateTime to);
}
