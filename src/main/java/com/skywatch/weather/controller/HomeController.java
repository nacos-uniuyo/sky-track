package com.skywatch.weather.controller;

import com.skywatch.weather.repository.SavedLocationRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    private final SavedLocationRepository savedLocationRepository;

    public HomeController(SavedLocationRepository savedLocationRepository) {
        this.savedLocationRepository = savedLocationRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("locations", savedLocationRepository.findAll());
        return "index";
    }
}
