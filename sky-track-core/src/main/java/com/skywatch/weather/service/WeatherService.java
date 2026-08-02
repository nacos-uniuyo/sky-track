package com.skywatch.weather.service;

import com.skywatch.weather.config.WeatherApiProperties;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse;
import com.skywatch.weather.dto.weather.ForecastResponse;
import com.skywatch.weather.dto.weather.GeoCityResponse;
import com.skywatch.weather.dto.weather.OneCallResponse;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

@Service
public class WeatherService {

    private static final String UNITS_METRIC = "metric";

    private final RestClient restClient;
    private final WeatherApiProperties properties;

    public WeatherService(RestClient weatherRestClient, WeatherApiProperties properties) {
        this.restClient = weatherRestClient;
        this.properties = properties;
    }

    public CurrentWeatherResponse currentWeather(BigDecimal lat, BigDecimal lon) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/weather")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("units", UNITS_METRIC)
                        .queryParam("appid", properties.getKey())
                        .build())
                .retrieve()
                .body(CurrentWeatherResponse.class);
    }

    public ForecastResponse forecast(BigDecimal lat, BigDecimal lon) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/2.5/forecast")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("units", UNITS_METRIC)
                        .queryParam("appid", properties.getKey())
                        .build())
                .retrieve()
                .body(ForecastResponse.class);
    }

    public OneCallResponse oneCall(BigDecimal lat, BigDecimal lon) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/data/3.0/onecall")
                        .queryParam("lat", lat)
                        .queryParam("lon", lon)
                        .queryParam("units", UNITS_METRIC)
                        .queryParam("exclude", "minutely")
                        .queryParam("appid", properties.getKey())
                        .build())
                .retrieve()
                .body(OneCallResponse.class);
    }

    public List<GeoCityResponse> searchCities(String query, int limit) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/geo/1.0/direct")
                        .queryParam("q", query)
                        .queryParam("limit", limit)
                        .queryParam("appid", properties.getKey())
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<List<GeoCityResponse>>() {
                });
    }
}
