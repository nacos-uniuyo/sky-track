package com.skywatch.weather.dto.weather;

import java.math.BigDecimal;
import java.util.List;

public record OneCallResponse(
        Current current,
        List<Hourly> hourly,
        List<Daily> daily,
        List<Alert> alerts
) {

    public record Current(
            Long dt,
            Long sunrise,
            Long sunset,
            BigDecimal temp,
            BigDecimal feels_like,
            Integer pressure,
            Integer humidity,
            BigDecimal wind_speed,
            Integer wind_deg,
            Integer visibility,
            List<WeatherInfo> weather
    ) {
    }

    public record Hourly(
            Long dt,
            BigDecimal temp,
            BigDecimal feels_like,
            Integer pressure,
            Integer humidity,
            BigDecimal wind_speed,
            List<WeatherInfo> weather,
            BigDecimal pop
    ) {
    }

    public record Daily(
            Long dt,
            Long sunrise,
            Long sunset,
            Temp temp,
            BigDecimal pop,
            List<WeatherInfo> weather
    ) {
    }

    public record Temp(
            BigDecimal day,
            BigDecimal min,
            BigDecimal max,
            BigDecimal night,
            BigDecimal eve,
            BigDecimal morn
    ) {
    }

    public record Alert(
            String sender_name,
            String event,
            Long start,
            Long end,
            String description
    ) {
    }

    public record WeatherInfo(Integer id, String main, String description, String icon) {
    }
}
