package com.gdc.tripmate.domain.phone.service;

public interface PhoneVerificationService {
    String sendVerificationCode(String phoneNumber);
    boolean verifyCode(String phoneNumber, String code);
}