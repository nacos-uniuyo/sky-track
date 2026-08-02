package com.skywatch.weather.repository;

import com.skywatch.weather.model.TrackedCity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrackedCityRepository extends JpaRepository<TrackedCity, Long> {

    List<TrackedCity> findByUserIdOrderBySortOrderAscIdAsc(Long userId);

    Optional<TrackedCity> findByUserIdAndId(Long userId, Long id);

    Optional<TrackedCity> findByUserIdAndPrimaryCityTrue(Long userId);

    Optional<TrackedCity> findFirstByUserIdAndPrimaryCityTrueOrderByCreatedAtAsc(Long userId);

    boolean existsByUserIdAndPrimaryCityTrue(Long userId);

    long countByUserId(Long userId);
}
