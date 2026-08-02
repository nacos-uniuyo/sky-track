package com.skywatch.weather.service;

import com.skywatch.weather.model.AppUser;
import java.time.LocalTime;
import org.springframework.stereotype.Service;

@Service
public class GreetingService {

    public String greeting(AppUser user, LocalTime now) {
        return "Hi comrade " + user.getFirstName() + ", " + partOfDay(now);
    }

    public String partOfDay(LocalTime now) {
        if (now.isAfter(LocalTime.of(21, 0)) || now.isBefore(LocalTime.of(5, 0))) {
            return "Good night";
        }
        if (now.isBefore(LocalTime.of(12, 0))) {
            return "Good morning";
        }
        if (now.isBefore(LocalTime.of(17, 0))) {
            return "Good afternoon";
        }
        return "Good evening";
    }

    public String themeClass(LocalTime now) {
        if (now.isAfter(LocalTime.of(21, 0)) || now.isBefore(LocalTime.of(5, 0))) {
            return "theme-night";
        }
        if (now.isBefore(LocalTime.of(12, 0))) {
            return "theme-morning";
        }
        if (now.isBefore(LocalTime.of(17, 0))) {
            return "theme-afternoon";
        }
        return "theme-evening";
    }
}
