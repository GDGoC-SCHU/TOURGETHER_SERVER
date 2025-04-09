package com.gdc.tripmate.global.security.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RefreshTokenRequest {
    private String refreshToken;
    private String email;
}