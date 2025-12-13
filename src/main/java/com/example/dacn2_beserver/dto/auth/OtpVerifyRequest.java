package com.example.dacn2_beserver.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpVerifyRequest {
    @NotBlank
    private String identifier;
    @NotBlank
    private String code;
    private String deviceId;
}