package com.skywatch.weather.service;

import com.skywatch.weather.dto.SignupRequest;
import com.skywatch.weather.model.AppUser;
import com.skywatch.weather.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public void createUser(SignupRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        if (appUserRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException();
        }

        AppUser user = new AppUser();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole("ROLE_USER");
        appUserRepository.save(user);
    }

    public static class EmailAlreadyRegisteredException extends RuntimeException {
    }
}
