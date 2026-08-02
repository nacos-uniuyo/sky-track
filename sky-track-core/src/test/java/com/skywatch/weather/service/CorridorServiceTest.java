package com.skywatch.weather.service;

import com.skywatch.weather.dto.CorridorBundle;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse.Main;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse.WeatherInfo;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorridorServiceTest {

    @Mock
    private WeatherService weatherService;

    @InjectMocks
    private CorridorService corridorService;

    @Test
    void generatesSixWaypointsWithWeather() {
        CurrentWeatherResponse weather = new CurrentWeatherResponse(
                null,
                List.of(new WeatherInfo(800, "Clear", "clear sky", "01d")),
                new Main(new BigDecimal("25.0"), null, null, null, null, null),
                null,
                null,
                null,
                "Lagos",
                null
        );
        when(weatherService.currentWeather(any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(weather);

        CorridorBundle bundle = corridorService.generate(
                "Lagos",
                new BigDecimal("6.5244"),
                new BigDecimal("3.3792"),
                "London",
                new BigDecimal("51.5072"),
                new BigDecimal("-0.1276")
        );

        assertThat(bundle.points()).hasSize(6);
        assertThat(bundle.points().get(0).label()).isEqualTo("Lagos");
        assertThat(bundle.points().get(5).label()).isEqualTo("London");
        assertThat(bundle.points().get(0).temperature()).isEqualByComparingTo("25.0");
        assertThat(bundle.points().get(0).conditionCode()).isEqualTo(800);
        assertThat(bundle.points().get(1).label()).isEqualTo("Stop 1");
    }

    @Test
    void weatherFailureYieldsNullReading() {
        when(weatherService.currentWeather(any(BigDecimal.class), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("api down"));

        CorridorBundle bundle = corridorService.generate(
                "A", new BigDecimal("0"), new BigDecimal("0"),
                "B", new BigDecimal("1"), new BigDecimal("1"));

        assertThat(bundle.points()).hasSize(6);
        assertThat(bundle.points().get(0).temperature()).isNull();
        assertThat(bundle.points().get(0).conditionCode()).isNull();
    }
}
