package com.skywatch.weather.service;

import com.skywatch.weather.dto.DeltaView;
import com.skywatch.weather.model.TrackedCity;
import com.skywatch.weather.model.WeatherSnapshot;
import com.skywatch.weather.repository.WeatherSnapshotRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeltaServiceTest {

    @Mock
    private WeatherSnapshotRepository weatherSnapshotRepository;

    @InjectMocks
    private DeltaService deltaService;

    private WeatherSnapshot snapshot(BigDecimal temperature, BigDecimal wind, Integer pressure, Integer humidity) {
        WeatherSnapshot snapshot = new WeatherSnapshot();
        snapshot.setTrackedCity(new TrackedCity());
        snapshot.setTemperature(temperature);
        snapshot.setWindSpeed(wind);
        snapshot.setPressure(pressure);
        snapshot.setHumidity(humidity);
        snapshot.setRecordedAt(OffsetDateTime.now());
        return snapshot;
    }

    @Test
    void deltaComparesLatestAgainstPrevious() {
        WeatherSnapshot latest = snapshot(new BigDecimal("24.0"), new BigDecimal("5.0"), 1012, 68);
        WeatherSnapshot previous = snapshot(new BigDecimal("28.0"), new BigDecimal("2.0"), 1020, 60);
        previous.setRecordedAt(latest.getRecordedAt().minusHours(3));

        when(weatherSnapshotRepository.findFirstByTrackedCityIdOrderByRecordedAtDesc(1L))
                .thenReturn(Optional.of(latest));
        when(weatherSnapshotRepository.findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(1L, latest.getRecordedAt()))
                .thenReturn(Optional.of(previous));

        Optional<DeltaView> delta = deltaService.deltaFor(1L);

        assertThat(delta).isPresent();
        assertThat(delta.get().temperatureDelta()).isEqualByComparingTo("-4.0");
        assertThat(delta.get().windSpeedDelta()).isEqualByComparingTo("3.0");
        assertThat(delta.get().pressureDelta()).isEqualTo(-8);
        assertThat(delta.get().humidityDelta()).isEqualTo(8);
        assertThat(delta.get().currentTemperature()).isEqualByComparingTo("24.0");
    }

    @Test
    void deltaEmptyWhenNoSnapshots() {
        when(weatherSnapshotRepository.findFirstByTrackedCityIdOrderByRecordedAtDesc(1L))
                .thenReturn(Optional.empty());

        Optional<DeltaView> delta = deltaService.deltaFor(1L);

        assertThat(delta).isEmpty();
    }

    @Test
    void deltaNullValuesWhenNoPrevious() {
        WeatherSnapshot latest = snapshot(new BigDecimal("24.0"), new BigDecimal("5.0"), 1012, 68);

        when(weatherSnapshotRepository.findFirstByTrackedCityIdOrderByRecordedAtDesc(1L))
                .thenReturn(Optional.of(latest));
        when(weatherSnapshotRepository.findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(1L, latest.getRecordedAt()))
                .thenReturn(Optional.empty());

        Optional<DeltaView> delta = deltaService.deltaFor(1L);

        assertThat(delta).isPresent();
        assertThat(delta.get().temperatureDelta()).isNull();
        assertThat(delta.get().currentTemperature()).isEqualByComparingTo("24.0");
    }
}
