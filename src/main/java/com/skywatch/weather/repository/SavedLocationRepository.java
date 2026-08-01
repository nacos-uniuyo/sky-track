package com.skywatch.weather.repository;

import com.skywatch.weather.model.SavedLocation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SavedLocationRepository extends JpaRepository<SavedLocation, Long> {
}
