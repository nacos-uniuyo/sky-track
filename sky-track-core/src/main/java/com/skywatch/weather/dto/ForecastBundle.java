package com.skywatch.weather.dto;

import java.util.List;

public record ForecastBundle(
        List<HourlyView> hourly,
        List<DailyView> daily
) {
}
