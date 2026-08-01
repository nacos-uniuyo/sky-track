package com.skywatch.weather.controller;

import com.skywatch.weather.dto.SignupRequest;
import com.skywatch.weather.service.SignupService;
import com.skywatch.weather.service.SignupService.EmailAlreadyRegisteredException;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final SignupService signupService;

    public AuthController(SignupService signupService) {
        this.signupService = signupService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String createAccount(
            @Valid @ModelAttribute SignupRequest signupRequest,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "auth/signup";
        }

        try {
            signupService.createUser(signupRequest);
        } catch (EmailAlreadyRegisteredException exception) {
            bindingResult.rejectValue("email", "email.exists", "An account already exists for this email.");
            return "auth/signup";
        }

        return "redirect:/login?registered";
    }
}
