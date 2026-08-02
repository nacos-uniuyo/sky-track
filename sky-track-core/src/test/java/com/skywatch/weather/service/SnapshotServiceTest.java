package com.skywatch.weather.service;

import com.skywatch.weather.dto.weather.CurrentWeatherResponse;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse.Coord;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse.Main;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse.Sys;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse.WeatherInfo;
import com.skywatch.weather.dto.weather.CurrentWeatherResponse.Wind;
import com.skywatch.weather.model.TrackedCity;
import com.skywatch.weather.model.WeatherSnapshot;
import com.skywatch.weather.repository.TrackedCityRepository;
import com.skywatch.weather.repository.WeatherSnapshotRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SnapshotServiceTest {

    @Mock
    private TrackedCityRepository trackedCityRepository;

    @Mock
    private WeatherSnapshotRepository weatherSnapshotRepository;

    @Mock
    private WeatherService weatherService;

    @Mock
    private WeatherAlertService weatherAlertService;

    @InjectMocks
    private SnapshotService snapshotService;

    @Test
    void mapsCurrentWeatherToSnapshot() {
        TrackedCity city = new TrackedCity();
        city.setCityName("Lagos");

        CurrentWeatherResponse weather = new CurrentWeatherResponse(
                new Coord(new BigDecimal("3.3792"), new BigDecimal("6.5244")),
                List.of(new WeatherInfo(804, "Clouds", "overcast clouds", "04d")),
                new Main(new BigDecimal("24.57"), new BigDecimal("25.3"), null, null, 1012, 85),
                10000,
                new Wind(new BigDecimal("5.73"), 180, null),
                new Sys(1754160000L, 1754200000L),
                "Lagos",
                1754200000L
        );
        when(weatherService.currentWeather(city.getLatitude(), city.getLongitude())).thenReturn(weather);
        when(weatherSnapshotRepository.save(any(WeatherSnapshot.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        snapshotService.ingestCity(city);

        ArgumentCaptor<WeatherSnapshot> captor = ArgumentCaptor.forClass(WeatherSnapshot.class);
        verify(weatherSnapshotRepository).save(captor.capture());
        WeatherSnapshot snapshot = captor.getValue();

        assertThat(snapshot.getTrackedCity()).isEqualTo(city);
        assertThat(snapshot.getTemperature()).isEqualByComparingTo("24.57");
        assertThat(snapshot.getHumidity()).isEqualTo(85);
        assertThat(snapshot.getPressure()).isEqualTo(1012);
        assertThat(snapshot.getWindSpeed()).isEqualByComparingTo("5.73");
        assertThat(snapshot.getWindDirection()).isEqualTo(180);
        assertThat(snapshot.getVisibility()).isEqualTo(10000);
        assertThat(snapshot.getConditionCode()).isEqualTo(804);
        assertThat(snapshot.getConditionText()).isEqualTo("overcast clouds");
        assertThat(snapshot.getSunrise()).isNotNull();
        assertThat(snapshot.getSunset()).isNotNull();
        assertThat(snapshot.getRecordedAt()).isNotNull();
    }
}
