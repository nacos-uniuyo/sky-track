package com.skywatch.weather.dto;

import com.skywatch.weather.dto.weather.ForecastResponse;
import com.skywatch.weather.dto.weather.OneCallResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

public record HourlyView(
        String time,
        Integer conditionCode,
        BigDecimal temperature
) {

    public static HourlyView from(OneCallResponse.Hourly hourly) {
        return new HourlyView(
                timeLabel(hourly.dt()),
                weatherCode(hourly.weather()),
                hourly.temp()
        );
    }

    public static HourlyView from(ForecastResponse.ForecastItem item) {
        return new HourlyView(
                timeLabel(item.dt()),
                weatherCode(item.weather()),
                item.main().temp()
        );
    }

    private static String timeLabel(Long epoch) {
        return Instant.ofEpochSecond(epoch)
                .atZone(ZoneOffset.UTC)
                .toLocalTime()
                .toString();
    }

    private static Integer weatherCode(List<?> weather) {
        if (weather instanceof List<?> list && !list.isEmpty()) {
            if (list.get(0) instanceof OneCallResponse.WeatherInfo info) {
                return info.id();
            }
            if (list.get(0) instanceof ForecastResponse.WeatherInfo info) {
                return info.id();
            }
        }
        return null;
    }
}
