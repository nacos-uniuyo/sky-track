package com.skywatch.weather.dto;

import java.math.BigDecimal;

public record CorridorPoint(
        String label,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer conditionCode,
        BigDecimal temperature
) {
}
