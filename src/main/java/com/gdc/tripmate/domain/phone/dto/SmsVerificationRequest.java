package com.gdc.tripmate.domain.phone.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SmsVerificationRequest {
    private String phoneNumber;
    private Long userId;
}