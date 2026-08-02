package com.skywatch.weather.dto;

import java.util.List;

public record CorridorBundle(
        String origin,
        String destination,
        List<CorridorPoint> points
) {
}
