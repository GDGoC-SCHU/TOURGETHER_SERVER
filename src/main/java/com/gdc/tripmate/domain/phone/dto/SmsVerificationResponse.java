package com.gdc.tripmate.domain.phone.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SmsVerificationResponse {
    private boolean success;
    private String message;
    private String code; // 개발용으로만 사용
    private int expiresInSeconds;
}