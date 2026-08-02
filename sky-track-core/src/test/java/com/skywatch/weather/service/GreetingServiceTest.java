package com.skywatch.weather.service;

import com.skywatch.weather.model.AppUser;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingServiceTest {

    private final GreetingService greetingService = new GreetingService();

    private AppUser user() {
        AppUser user = new AppUser();
        user.setFirstName("John");
        user.setLastName("Doe");
        return user;
    }

    @Test
    void morningGreeting() {
        assertThat(greetingService.greeting(user(), LocalTime.of(9, 0))).isEqualTo("Hi comrade John, Good morning");
    }

    @Test
    void afternoonGreeting() {
        assertThat(greetingService.greeting(user(), LocalTime.of(14, 0))).isEqualTo("Hi comrade John, Good afternoon");
    }

    @Test
    void eveningGreeting() {
        assertThat(greetingService.greeting(user(), LocalTime.of(19, 0))).isEqualTo("Hi comrade John, Good evening");
    }

    @Test
    void nightGreeting() {
        assertThat(greetingService.greeting(user(), LocalTime.of(22, 0))).isEqualTo("Hi comrade John, Good night");
    }

    @Test
    void earlyMorningNightGreeting() {
        assertThat(greetingService.greeting(user(), LocalTime.of(3, 0))).isEqualTo("Hi comrade John, Good night");
    }
}
