package com.gdc.tripmate.domain.phone.dto;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class FirebasePhoneRequest {
    private Long userId;
    private String phoneNumber;
    private String verificationId;
    private String verificationCode;
}