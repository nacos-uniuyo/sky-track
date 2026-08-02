package com.skywatch.weather.service;

import com.skywatch.weather.dto.TrackedCityRequest;
import com.skywatch.weather.model.AppUser;
import com.skywatch.weather.model.TrackedCity;
import com.skywatch.weather.repository.TrackedCityRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TrackedCityService {

    private final TrackedCityRepository trackedCityRepository;

    public TrackedCityService(TrackedCityRepository trackedCityRepository) {
        this.trackedCityRepository = trackedCityRepository;
    }

    public List<TrackedCity> listCities(AppUser user) {
        return trackedCityRepository.findByUserIdOrderBySortOrderAscIdAsc(user.getId());
    }

    @Transactional
    public TrackedCity addCity(AppUser user, TrackedCityRequest request) {
        boolean hasPrimary = trackedCityRepository.existsByUserIdAndPrimaryCityTrue(user.getId());
        int nextOrder = (int) trackedCityRepository.countByUserId(user.getId()) + 1;

        TrackedCity city = new TrackedCity();
        city.setUser(user);
        city.setCityName(request.cityName());
        city.setCountry(request.country());
        city.setCountryCode(request.countryCode());
        city.setLatitude(request.latitude());
        city.setLongitude(request.longitude());
        city.setPrimaryCity(!hasPrimary);
        city.setSortOrder(nextOrder);
        return trackedCityRepository.save(city);
    }

    @Transactional
    public void removeCity(AppUser user, Long cityId) {
        Optional<TrackedCity> target = trackedCityRepository.findByUserIdAndId(user.getId(), cityId);
        if (target.isEmpty()) {
            return;
        }
        boolean wasPrimary = target.get().isPrimaryCity();
        trackedCityRepository.delete(target.get());
        if (wasPrimary) {
            trackedCityRepository.findByUserIdOrderBySortOrderAscIdAsc(user.getId())
                    .stream()
                    .findFirst()
                    .ifPresent(city -> city.setPrimaryCity(true));
        }
    }

    @Transactional
    public void setPrimary(AppUser user, Long cityId) {
        Optional<TrackedCity> target = trackedCityRepository.findByUserIdAndId(user.getId(), cityId);
        if (target.isEmpty()) {
            return;
        }
        trackedCityRepository.findByUserIdAndPrimaryCityTrue(user.getId())
                .filter(city -> !city.getId().equals(cityId))
                .ifPresent(city -> city.setPrimaryCity(false));
        target.get().setPrimaryCity(true);
        trackedCityRepository.flush();
    }

    @Transactional
    public void reorder(AppUser user, List<Long> cityIds) {
        List<TrackedCity> cities = listCities(user);
        for (int index = 0; index < cityIds.size(); index++) {
            Long cityId = cityIds.get(index);
            int newOrder = index + 1;
            cities.stream()
                    .filter(city -> city.getId().equals(cityId))
                    .findFirst()
                    .ifPresent(city -> city.setSortOrder(newOrder));
        }
    }

    @Transactional
    public TrackedCity savePrimaryCity(AppUser user, TrackedCityRequest request) {
        trackedCityRepository.findByUserIdAndPrimaryCityTrue(user.getId())
                .ifPresent(city -> city.setPrimaryCity(false));
        int nextOrder = (int) trackedCityRepository.countByUserId(user.getId()) + 1;

        TrackedCity city = new TrackedCity();
        city.setUser(user);
        city.setCityName(request.cityName());
        city.setCountry(request.country());
        city.setCountryCode(request.countryCode());
        city.setLatitude(request.latitude());
        city.setLongitude(request.longitude());
        city.setPrimaryCity(true);
        city.setSortOrder(nextOrder);
        return trackedCityRepository.save(city);
    }

    public Optional<TrackedCity> findPrimaryCity(AppUser user) {
        return trackedCityRepository.findFirstByUserIdAndPrimaryCityTrueOrderByCreatedAtAsc(user.getId());
    }

    public Optional<TrackedCity> findCity(AppUser user, Long cityId) {
        return trackedCityRepository.findByUserIdAndId(user.getId(), cityId);
    }

    public long countByUser(AppUser user) {
        return trackedCityRepository.countByUserId(user.getId());
    }
}
