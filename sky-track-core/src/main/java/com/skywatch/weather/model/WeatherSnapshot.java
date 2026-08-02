package com.skywatch.weather.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "weather_snapshots")
public class WeatherSnapshot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tracked_city_id", nullable = false)
    private TrackedCity trackedCity;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "feels_like", precision = 5, scale = 2)
    private BigDecimal feelsLike;

    @Column
    private Integer humidity;

    @Column(name = "wind_speed", precision = 5, scale = 2)
    private BigDecimal windSpeed;

    @Column(name = "wind_direction")
    private Integer windDirection;

    @Column
    private Integer pressure;

    @Column
    private Integer visibility;

    @Column(name = "condition_code")
    private Integer conditionCode;

    @Column(name = "condition_text", length = 120)
    private String conditionText;

    @Column
    private OffsetDateTime sunrise;

    @Column
    private OffsetDateTime sunset;

    @Column(name = "recorded_at", nullable = false)
    private OffsetDateTime recordedAt;

    public Long getId() {
        return id;
    }

    public TrackedCity getTrackedCity() {
        return trackedCity;
    }

    public void setTrackedCity(TrackedCity trackedCity) {
        this.trackedCity = trackedCity;
    }

    public BigDecimal getTemperature() {
        return temperature;
    }

    public void setTemperature(BigDecimal temperature) {
        this.temperature = temperature;
    }

    public BigDecimal getFeelsLike() {
        return feelsLike;
    }

    public void setFeelsLike(BigDecimal feelsLike) {
        this.feelsLike = feelsLike;
    }

    public Integer getHumidity() {
        return humidity;
    }

    public void setHumidity(Integer humidity) {
        this.humidity = humidity;
    }

    public BigDecimal getWindSpeed() {
        return windSpeed;
    }

    public void setWindSpeed(BigDecimal windSpeed) {
        this.windSpeed = windSpeed;
    }

    public Integer getWindDirection() {
        return windDirection;
    }

    public void setWindDirection(Integer windDirection) {
        this.windDirection = windDirection;
    }

    public Integer getPressure() {
        return pressure;
    }

    public void setPressure(Integer pressure) {
        this.pressure = pressure;
    }

    public Integer getVisibility() {
        return visibility;
    }

    public void setVisibility(Integer visibility) {
        this.visibility = visibility;
    }

    public Integer getConditionCode() {
        return conditionCode;
    }

    public void setConditionCode(Integer conditionCode) {
        this.conditionCode = conditionCode;
    }

    public String getConditionText() {
        return conditionText;
    }

    public void setConditionText(String conditionText) {
        this.conditionText = conditionText;
    }

    public OffsetDateTime getSunrise() {
        return sunrise;
    }

    public void setSunrise(OffsetDateTime sunrise) {
        this.sunrise = sunrise;
    }

    public OffsetDateTime getSunset() {
        return sunset;
    }

    public void setSunset(OffsetDateTime sunset) {
        this.sunset = sunset;
    }

    public OffsetDateTime getRecordedAt() {
        return recordedAt;
    }

    public void setRecordedAt(OffsetDateTime recordedAt) {
        this.recordedAt = recordedAt;
    }
}
