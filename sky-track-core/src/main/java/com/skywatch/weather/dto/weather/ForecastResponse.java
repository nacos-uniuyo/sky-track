package com.skywatch.weather.dto.weather;

import java.math.BigDecimal;
import java.util.List;

public record ForecastResponse(
        List<ForecastItem> list,
        City city
) {

    public record ForecastItem(
            Long dt,
            Main main,
            List<WeatherInfo> weather,
            Wind wind,
            Clouds clouds
    ) {
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

    public record WeatherInfo(Integer id, String main, String description, String icon) {
    }

    public record Wind(BigDecimal speed, Integer deg, BigDecimal gust) {
    }

    public record Clouds(Integer all) {
    }

    public record City(Long id, String name, String country, Coord coord) {
    }

    public record Coord(BigDecimal lon, BigDecimal lat) {
    }
}
