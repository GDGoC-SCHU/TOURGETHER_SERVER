package com.gdc.tripmate.domain.phone.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VerifyCodeRequest {
    private Long userId;
    private String phoneNumber;
    private String code;
}