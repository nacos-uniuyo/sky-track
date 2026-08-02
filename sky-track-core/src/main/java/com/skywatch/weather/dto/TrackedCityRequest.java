package com.skywatch.weather.dto;

import java.math.BigDecimal;

public record TrackedCityRequest(
        String cityName,
        String country,
        String countryCode,
        BigDecimal latitude,
        BigDecimal longitude
) {
}
