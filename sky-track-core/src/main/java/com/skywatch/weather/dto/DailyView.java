package com.skywatch.weather.dto;

import com.skywatch.weather.dto.weather.ForecastResponse;
import com.skywatch.weather.dto.weather.OneCallResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

public record DailyView(
        String day,
        Integer conditionCode,
        BigDecimal tempMin,
        BigDecimal tempMax,
        BigDecimal precipitation
) {

    public static DailyView from(OneCallResponse.Daily daily) {
        LocalDate date = Instant.ofEpochSecond(daily.dt())
                .atZone(ZoneOffset.UTC)
                .toLocalDate();
        Integer conditionCode = !daily.weather().isEmpty() ? daily.weather().get(0).id() : null;
        return new DailyView(
                date.getDayOfWeek().toString(),
                conditionCode,
                daily.temp().min(),
                daily.temp().max(),
                daily.pop()
        );
    }

    public static DailyView fromGroup(LocalDate date, List<ForecastResponse.ForecastItem> items) {
        BigDecimal min = items.stream()
                .map(item -> item.main().temp_min())
                .min(BigDecimal::compareTo)
                .orElse(null);
        BigDecimal max = items.stream()
                .map(item -> item.main().temp_max())
                .max(BigDecimal::compareTo)
                .orElse(null);
        Integer conditionCode = !items.isEmpty() && !items.get(0).weather().isEmpty()
                ? items.get(0).weather().get(0).id() : null;
        return new DailyView(
                date.getDayOfWeek().toString(),
                conditionCode,
                min,
                max,
                null
        );
    }
}
