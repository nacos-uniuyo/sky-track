package com.skywatch.weather.controller;

import com.skywatch.weather.config.CurrentUserResolver;
import com.skywatch.weather.dto.TrackedCityView;
import com.skywatch.weather.dto.weather.GeoCityResponse;
import com.skywatch.weather.model.AppUser;
import com.skywatch.weather.model.TrackedCity;
import com.skywatch.weather.model.WeatherAlert;
import com.skywatch.weather.repository.WeatherAlertRepository;
import com.skywatch.weather.service.ContextualMessageService;
import com.skywatch.weather.service.DashboardService;
import com.skywatch.weather.service.GreetingService;
import com.skywatch.weather.service.TrackedCityService;
import com.skywatch.weather.service.WeatherService;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HomeController {

    private final CurrentUserResolver currentUserResolver;
    private final GreetingService greetingService;
    private final TrackedCityService trackedCityService;
    private final ContextualMessageService contextualMessageService;
    private final DashboardService dashboardService;
    private final WeatherService weatherService;
    private final WeatherAlertRepository weatherAlertRepository;

    public HomeController(
            CurrentUserResolver currentUserResolver,
            GreetingService greetingService,
            TrackedCityService trackedCityService,
            ContextualMessageService contextualMessageService,
            DashboardService dashboardService,
            WeatherService weatherService,
            WeatherAlertRepository weatherAlertRepository
    ) {
        this.currentUserResolver = currentUserResolver;
        this.greetingService = greetingService;
        this.trackedCityService = trackedCityService;
        this.contextualMessageService = contextualMessageService;
        this.dashboardService = dashboardService;
        this.weatherService = weatherService;
        this.weatherAlertRepository = weatherAlertRepository;
    }

    @GetMapping("/")
    public String index(
            Authentication authentication,
            @RequestParam(required = false) String q,
            Model model
    ) {
        AppUser user = currentUserResolver.currentUser(authentication);
        model.addAttribute("greeting", greetingService.greeting(user, LocalTime.now()));
        model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
        model.addAttribute("firstName", user.getFirstName());
        model.addAttribute("alerts", weatherAlertRepository.findAllByTrackedCityUserIdOrderByDetectedAtDesc(user.getId()));

        List<TrackedCityView> cities = dashboardService.cityViews(user);
        model.addAttribute("cities", cities);
        model.addAttribute("primaryCityView", dashboardService.primaryCityView(user));

        Optional<TrackedCity> primary = trackedCityService.findPrimaryCity(user);
        if (primary.isPresent()) {
            TrackedCity city = primary.get();
            TrackedCityView primaryView = dashboardService.primaryCityView(user);
            if (primaryView != null) {
                model.addAttribute("cityMessage", contextualMessageService.messageFor(
                        city.getCityName(),
                        primaryView.conditionCode(),
                        primaryView.temperature(),
                        primaryView.windSpeed()));
            } else {
                model.addAttribute("cityMessage", contextualMessageService.messageFor(
                        city.getCityName(), null, null, null));
            }
            model.addAttribute("forecast", dashboardService.forecastFor(city));
            model.addAttribute("history", dashboardService.historyFor(city, 7));
        } else {
            model.addAttribute("cityMessage", null);
            model.addAttribute("forecast", null);
            model.addAttribute("history", List.of());
        }

        if (q != null && !q.isBlank()) {
            List<GeoCityResponse> searchResults = weatherService.searchCities(q, 5);
            model.addAttribute("searchResults", searchResults);
            model.addAttribute("searchQuery", q);
        }

        return "index";
    }
}
