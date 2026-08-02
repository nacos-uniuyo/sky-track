package com.skywatch.weather.service;

import com.skywatch.weather.model.TrackedCity;
import com.skywatch.weather.model.WeatherAlert;
import com.skywatch.weather.model.WeatherSnapshot;
import com.skywatch.weather.repository.WeatherAlertRepository;
import com.skywatch.weather.repository.WeatherSnapshotRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WeatherAlertServiceTest {

    @Mock
    private WeatherAlertRepository weatherAlertRepository;

    @Mock
    private WeatherSnapshotRepository weatherSnapshotRepository;

    @InjectMocks
    private WeatherAlertService weatherAlertService;

    private TrackedCity city() {
        TrackedCity city = new TrackedCity();
        city.setCityName("Lagos");
        return city;
    }

    @Test
    void createsPressureDropAlert() {
        TrackedCity city = city();
        WeatherSnapshot latest = snapshot(1010, BigDecimal.valueOf(4));
        WeatherSnapshot previous = snapshot(1020, BigDecimal.valueOf(4));
        previous.setRecordedAt(latest.getRecordedAt().minusHours(3));

        when(weatherSnapshotRepository.findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(
                isNull(), any())).thenReturn(Optional.of(previous));

        weatherAlertService.evaluate(city, latest);

        ArgumentCaptor<WeatherAlert> captor = ArgumentCaptor.forClass(WeatherAlert.class);
        verify(weatherAlertRepository).save(captor.capture());
        assertThat(captor.getValue().getAlertType()).isEqualTo("PRESSURE_DROP");
        assertThat(captor.getValue().getSeverity()).isEqualTo("HIGH");
    }

    @Test
    void noAlertWhenPressureStable() {
        TrackedCity city = city();
        WeatherSnapshot latest = snapshot(1018, BigDecimal.valueOf(4));
        WeatherSnapshot previous = snapshot(1020, BigDecimal.valueOf(4));
        previous.setRecordedAt(latest.getRecordedAt().minusHours(3));

        when(weatherSnapshotRepository.findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(
                isNull(), any())).thenReturn(Optional.of(previous));

        weatherAlertService.evaluate(city, latest);

        verify(weatherAlertRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void noAlertWhenFirstSnapshot() {
        TrackedCity city = city();
        WeatherSnapshot latest = snapshot(1010, BigDecimal.valueOf(4));

        when(weatherSnapshotRepository.findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(
                isNull(), any())).thenReturn(Optional.empty());

        weatherAlertService.evaluate(city, latest);

        verify(weatherAlertRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    private WeatherSnapshot snapshot(Integer pressure, BigDecimal windSpeed) {
        WeatherSnapshot snapshot = new WeatherSnapshot();
        snapshot.setPressure(pressure);
        snapshot.setWindSpeed(windSpeed);
        snapshot.setRecordedAt(OffsetDateTime.now());
        return snapshot;
    }
}
