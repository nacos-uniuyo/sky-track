package com.skywatch.weather.dto;

import com.skywatch.weather.dto.weather.CurrentWeatherResponse;
import com.skywatch.weather.model.TrackedCity;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TrackedCityView(
        Long id,
        String cityName,
        String country,
        String countryCode,
        BigDecimal latitude,
        BigDecimal longitude,
        boolean primaryCity,
        Integer conditionCode,
        String conditionText,
        BigDecimal temperature,
        BigDecimal feelsLike,
        BigDecimal tempMin,
        BigDecimal tempMax,
        Integer humidity,
        BigDecimal windSpeed,
        Integer windDirection,
        Integer pressure,
        BigDecimal temperatureDelta,
        OffsetDateTime deltaSince
) {

    public static TrackedCityView from(TrackedCity city, CurrentWeatherResponse weather) {
        BigDecimal temperature = weather != null && weather.main() != null ? weather.main().temp() : null;
        BigDecimal feelsLike = weather != null && weather.main() != null ? weather.main().feels_like() : null;
        BigDecimal tempMin = weather != null && weather.main() != null ? weather.main().temp_min() : null;
        BigDecimal tempMax = weather != null && weather.main() != null ? weather.main().temp_max() : null;
        Integer humidity = weather != null && weather.main() != null ? weather.main().humidity() : null;
        Integer pressure = weather != null && weather.main() != null ? weather.main().pressure() : null;
        BigDecimal windSpeed = weather != null && weather.wind() != null ? weather.wind().speed() : null;
        Integer windDirection = weather != null && weather.wind() != null ? weather.wind().deg() : null;
        Integer conditionCode = weather != null && !weather.weather().isEmpty()
                ? weather.weather().get(0).id() : null;
        String conditionText = weather != null && !weather.weather().isEmpty()
                ? weather.weather().get(0).description() : null;

        return new TrackedCityView(
                city.getId(),
                city.getCityName(),
                city.getCountry(),
                city.getCountryCode(),
                city.getLatitude(),
                city.getLongitude(),
                city.isPrimaryCity(),
                conditionCode,
                conditionText,
                temperature,
                feelsLike,
                tempMin,
                tempMax,
                humidity,
                windSpeed,
                windDirection,
                pressure,
                null,
                null
        );
    }

    public TrackedCityView withDelta(BigDecimal temperatureDelta, OffsetDateTime deltaSince) {
        return new TrackedCityView(
                id,
                cityName,
                country,
                countryCode,
                latitude,
                longitude,
                primaryCity,
                conditionCode,
                conditionText,
                temperature,
                feelsLike,
                tempMin,
                tempMax,
                humidity,
                windSpeed,
                windDirection,
                pressure,
                temperatureDelta,
                deltaSince
        );
    }
}
