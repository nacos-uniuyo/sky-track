package com.skywatch.weather.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record SnapshotView(
        OffsetDateTime recordedAt,
        BigDecimal temperature
) {
}
