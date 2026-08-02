package com.skywatch.weather.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record DeltaView(
        BigDecimal temperatureDelta,
        BigDecimal windSpeedDelta,
        Integer pressureDelta,
        Integer humidityDelta,
        OffsetDateTime since,
        BigDecimal currentTemperature,
        BigDecimal currentWindSpeed
) {
}
