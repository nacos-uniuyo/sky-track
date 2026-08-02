package com.skywatch.weather.service;

import java.math.BigDecimal;
import org.springframework.stereotype.Service;

@Service
public class ContextualMessageService {

    public String messageFor(String city, Integer conditionCode, BigDecimal temperature, BigDecimal windSpeed) {
        Category category = categorize(conditionCode, temperature, windSpeed);
        return switch (category) {
            case RAIN -> "Rain is heading toward " + city + " - pack an umbrella if you are visiting.";
            case STORM -> "Storm watch active over " + city + " - consider rescheduling your trip.";
            case HEAT -> "It is hot over " + city + " today - stay hydrated if you head out.";
            case WINDY -> "Wind is picking up in " + city + " - light layers recommended.";
            case CLOUDY -> "Clouds are rolling over " + city + ", but no rain expected - a good day to visit.";
            case CLEAR -> "Skies over " + city + " are calm and dry - safe to visit when you are ready.";
            case UNKNOWN -> "When are you visiting " + city + "? Weather updates are on the way.";
        };
    }

    private Category categorize(Integer conditionCode, BigDecimal temperature, BigDecimal windSpeed) {
        if (conditionCode == null) {
            return Category.UNKNOWN;
        }
        if (conditionCode >= 200 && conditionCode < 300) {
            return Category.STORM;
        }
        if (conditionCode >= 300 && conditionCode < 600) {
            return Category.RAIN;
        }
        if (conditionCode >= 600 && conditionCode < 700) {
            return Category.RAIN;
        }
        if (conditionCode >= 700 && conditionCode < 800) {
            return Category.CLOUDY;
        }
        if (conditionCode == 800) {
            if (temperature != null && temperature.compareTo(BigDecimal.valueOf(32)) >= 0) {
                return Category.HEAT;
            }
            return Category.CLEAR;
        }
        if (conditionCode > 800 && conditionCode < 900) {
            if (windSpeed != null && windSpeed.compareTo(BigDecimal.valueOf(12)) >= 0) {
                return Category.WINDY;
            }
            return Category.CLOUDY;
        }
        return Category.UNKNOWN;
    }

    private enum Category {
        RAIN,
        STORM,
        HEAT,
        WINDY,
        CLOUDY,
        CLEAR,
        UNKNOWN
    }
}
