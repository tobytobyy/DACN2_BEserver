package com.example.dacn2_beserver.service;

import com.example.dacn2_beserver.dto.auth.RegisterRequest;
import com.example.dacn2_beserver.exception.EmailAlreadyExistsException;
import com.example.dacn2_beserver.exception.UserNotFoundException;
import com.example.dacn2_beserver.exception.UsernameAlreadyExistsException;
import com.example.dacn2_beserver.model.user.*;
import com.example.dacn2_beserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        if (userRepository.existsByUsername(request.username())) {
            throw new UsernameAlreadyExistsException(request.username());
        }

        Instant now = Instant.now();

        User user = User.builder()
                .email(request.email())
                .username(request.username())
                .passwordHash(passwordEncoder.encode(request.password()))
                .provider(AuthProvider.LOCAL)
                .roles(List.of("USER"))
                .profile(UserProfile.builder().build())
                .goals(defaultGoals())
                .settings(defaultSettings())
                .connectedServices(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        return userRepository.save(user);
    }

    public User getById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    public User getByEmailOrUsername(String usernameOrEmail) {
        // login: cho phép dùng email hoặc username
        return userRepository.findByEmail(usernameOrEmail)
                .or(() -> userRepository.findByUsername(usernameOrEmail))
                .orElseThrow(() -> new UserNotFoundException("User not found with: " + usernameOrEmail));
    }

    private UserGoals defaultGoals() {
        return UserGoals.builder()
                .dailySteps(8000)
                .dailyCaloriesIn(2000)
                .dailyCaloriesOut(2200)
                .dailyWaterMl(2000)
                .build();
    }

    private UserSettings defaultSettings() {
        return UserSettings.builder()
                .unitSystem(UnitSystem.METRIC)
                .language("vi")
                .notifications(NotificationSettings.builder()
                        .enabled(true)
                        .remindDrinkWater(true)
                        .remindSleep(true)
                        .build())
                .build();
    }
}
