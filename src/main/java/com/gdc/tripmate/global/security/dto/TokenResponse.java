package com.gdc.tripmate.global.security.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private boolean needPhoneVerification;
    private Long userId;
}