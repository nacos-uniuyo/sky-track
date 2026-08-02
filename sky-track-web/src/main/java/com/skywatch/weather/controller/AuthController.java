package com.skywatch.weather.controller;

import com.skywatch.weather.dto.SignupRequest;
import com.skywatch.weather.service.GreetingService;
import com.skywatch.weather.service.SignupService;
import com.skywatch.weather.service.SignupService.EmailAlreadyRegisteredException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.LocalTime;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final SignupService signupService;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;
    private final GreetingService greetingService;

    public AuthController(
            SignupService signupService,
            AuthenticationManager authenticationManager,
            GreetingService greetingService
    ) {
        this.signupService = signupService;
        this.authenticationManager = authenticationManager;
        this.greetingService = greetingService;
        this.securityContextRepository = new HttpSessionSecurityContextRepository();
    }

    @GetMapping("/login")
    public String login(Model model) {
        model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
        return "auth/login";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("signupRequest", new SignupRequest());
        model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
        return "auth/signup";
    }

    @PostMapping("/signup")
    public String createAccount(
            @Valid @ModelAttribute SignupRequest signupRequest,
            BindingResult bindingResult,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
            return "auth/signup";
        }

        try {
            signupService.createUser(signupRequest);
        } catch (EmailAlreadyRegisteredException exception) {
            bindingResult.rejectValue("email", "email.exists", "An account already exists for this email.");
            model.addAttribute("themeClass", greetingService.themeClass(LocalTime.now()));
            return "auth/signup";
        }

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(signupRequest.getEmail(), signupRequest.getPassword()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return "redirect:/onboarding";
    }
}
