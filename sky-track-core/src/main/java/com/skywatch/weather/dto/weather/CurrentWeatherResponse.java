package com.skywatch.weather.dto.weather;

import java.math.BigDecimal;
import java.util.List;

public record CurrentWeatherResponse(
        Coord coord,
        List<WeatherInfo> weather,
        Main main,
        Integer visibility,
        Wind wind,
        Sys sys,
        String name,
        Long dt
) {

    public record Coord(BigDecimal lon, BigDecimal lat) {
    }

    public record WeatherInfo(Integer id, String main, String description, String icon) {
    }

    public record Main(
            BigDecimal temp,
            BigDecimal feels_like,
            BigDecimal temp_min,
            BigDecimal temp_max,
            Integer pressure,
            Integer humidity
    ) {
    }

    public record Wind(BigDecimal speed, Integer deg, BigDecimal gust) {
    }

    public record Sys(Long sunrise, Long sunset) {
    }
}
