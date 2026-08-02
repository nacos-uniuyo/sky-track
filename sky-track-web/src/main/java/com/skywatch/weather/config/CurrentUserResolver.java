package com.skywatch.weather.config;

import com.skywatch.weather.model.AppUser;
import com.skywatch.weather.repository.AppUserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class CurrentUserResolver {

    private final AppUserRepository appUserRepository;

    public CurrentUserResolver(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    public AppUser currentUser(Authentication authentication) {
        return appUserRepository.findByEmailIgnoreCase(authentication.getName()).orElseThrow();
    }
}
