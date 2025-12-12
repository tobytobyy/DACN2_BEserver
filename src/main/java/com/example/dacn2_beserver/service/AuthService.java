package com.example.dacn2_beserver.service;

import com.example.dacn2_beserver.dto.auth.AuthResponse;
import com.example.dacn2_beserver.dto.auth.LoginRequest;
import com.example.dacn2_beserver.dto.auth.RegisterRequest;
import com.example.dacn2_beserver.exception.InvalidCredentialsException;
import com.example.dacn2_beserver.model.user.User;
import com.example.dacn2_beserver.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public User register(RegisterRequest request) {
        return userService.register(request);
    }

    public AuthResponse login(LoginRequest request) {
        // cho phép login bằng email hoặc username
        User user = userService.getByEmailOrUsername(request.usernameOrEmail());

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = jwtTokenProvider.generateToken(user.getId(), user.getUsername());

        return new AuthResponse(token);
    }
}
