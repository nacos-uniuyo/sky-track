package com.skywatch.weather.controller;

import com.skywatch.weather.config.CurrentUserResolver;
import com.skywatch.weather.model.AppUser;
import com.skywatch.weather.repository.WeatherAlertRepository;
import com.skywatch.weather.service.GreetingService;
import java.time.LocalTime;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AlertsController {

    private final CurrentUserResolver currentUserResolver;
    private final GreetingService greetingService;
    private final WeatherAlertRepository weatherAlertRepository;

    public AlertsController(
            CurrentUserResolver currentUserResolver,
            GreetingService greetingService,
            WeatherAlertRepository weatherAlertRepository
    ) {
        this.currentUserResolver = currentUserResolver;
        this.greetingService = greetingService;
        this.weatherAlertRepository = weatherAlertRepository;
    }

    @GetMapping("/alerts")
    public String alerts(Authentication authentication, Model model) {
        AppUser user = currentUserResolver.currentUser(authentication);
        model.addAttribute("greeting", greetingService.greeting(user, LocalTime.now()));
        model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
        model.addAttribute("alerts", weatherAlertRepository.findAllByTrackedCityUserIdOrderByDetectedAtDesc(user.getId()));
        return "alerts";
    }

    @PostMapping("/alerts/{id}/acknowledge")
    public String acknowledge(Authentication authentication, @PathVariable Long id) {
        AppUser user = currentUserResolver.currentUser(authentication);
        if (weatherAlertRepository.existsByIdAndTrackedCityUserId(id, user.getId())) {
            weatherAlertRepository.findById(id).ifPresent(alert -> {
                alert.setAcknowledged(true);
                weatherAlertRepository.save(alert);
            });
        }
        return "redirect:/alerts";
    }
}
