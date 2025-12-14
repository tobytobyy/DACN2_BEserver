package com.example.dacn2_beserver.controller;

import com.example.dacn2_beserver.dto.auth.OtpRequestCreateRequest;
import com.example.dacn2_beserver.dto.auth.OtpRequestCreateResponse;
import com.example.dacn2_beserver.dto.auth.OtpVerifyRequest;
import com.example.dacn2_beserver.dto.auth.OtpVerifyResponse;
import com.example.dacn2_beserver.service.auth.OtpAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final OtpAuthService otpAuthService;

    @PostMapping("/otp/request")
    public OtpRequestCreateResponse requestOtp(@Valid @RequestBody OtpRequestCreateRequest req) {
        return otpAuthService.requestOtp(req);
    }

    @PostMapping("/otp/verify")
    public OtpVerifyResponse verifyOtp(@Valid @RequestBody OtpVerifyRequest req) {
        return otpAuthService.verifyOtp(req);
    }
}