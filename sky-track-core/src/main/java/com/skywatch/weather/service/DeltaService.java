package com.skywatch.weather.service;

import com.skywatch.weather.dto.DeltaView;
import com.skywatch.weather.model.WeatherSnapshot;
import com.skywatch.weather.repository.WeatherSnapshotRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class DeltaService {

    private final WeatherSnapshotRepository weatherSnapshotRepository;

    public DeltaService(WeatherSnapshotRepository weatherSnapshotRepository) {
        this.weatherSnapshotRepository = weatherSnapshotRepository;
    }

    public Optional<DeltaView> deltaFor(Long trackedCityId) {
        Optional<WeatherSnapshot> latestOpt = weatherSnapshotRepository
                .findFirstByTrackedCityIdOrderByRecordedAtDesc(trackedCityId);
        if (latestOpt.isEmpty()) {
            return Optional.empty();
        }

        WeatherSnapshot latest = latestOpt.get();
        Optional<WeatherSnapshot> previousOpt = weatherSnapshotRepository
                .findFirstByTrackedCityIdAndRecordedAtBeforeOrderByRecordedAtDesc(
                        trackedCityId, latest.getRecordedAt());

        BigDecimal tempDelta = null;
        BigDecimal windDelta = null;
        Integer pressureDelta = null;
        Integer humidityDelta = null;
        OffsetDateTime since = latest.getRecordedAt();

        if (previousOpt.isPresent()) {
            WeatherSnapshot previous = previousOpt.get();
            tempDelta = subtract(latest.getTemperature(), previous.getTemperature());
            windDelta = subtract(latest.getWindSpeed(), previous.getWindSpeed());
            pressureDelta = subtractInt(latest.getPressure(), previous.getPressure());
            humidityDelta = subtractInt(latest.getHumidity(), previous.getHumidity());
            since = previous.getRecordedAt();
        }

        return Optional.of(new DeltaView(
                tempDelta,
                windDelta,
                pressureDelta,
                humidityDelta,
                since,
                latest.getTemperature(),
                latest.getWindSpeed()
        ));
    }

    private BigDecimal subtract(BigDecimal latest, BigDecimal previous) {
        if (latest == null || previous == null) {
            return null;
        }
        return latest.subtract(previous);
    }

    private Integer subtractInt(Integer latest, Integer previous) {
        if (latest == null || previous == null) {
            return null;
        }
        return latest - previous;
    }
}
