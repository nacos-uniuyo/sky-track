package com.skywatch.weather.controller;

import com.skywatch.weather.model.SavedLocation;
import com.skywatch.weather.repository.SavedLocationRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SavedLocationController {

    private final SavedLocationRepository savedLocationRepository;

    public SavedLocationController(SavedLocationRepository savedLocationRepository) {
        this.savedLocationRepository = savedLocationRepository;
    }

    @PostMapping("/locations")
    public String saveLocation(@Valid @ModelAttribute SavedLocation savedLocation) {
        savedLocationRepository.save(savedLocation);
        return "redirect:/";
    }
}
