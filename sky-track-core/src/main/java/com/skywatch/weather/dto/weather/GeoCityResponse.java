package com.skywatch.weather.dto.weather;

import java.math.BigDecimal;

public record GeoCityResponse(
        String name,
        BigDecimal lat,
        BigDecimal lon,
        String country,
        String state
) {
}
