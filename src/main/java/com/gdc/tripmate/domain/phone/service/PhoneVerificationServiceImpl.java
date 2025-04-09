package com.gdc.tripmate.domain.phone.service;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PhoneVerificationServiceImpl implements PhoneVerificationService {
    
    private final RedisTemplate<String, String> redisTemplate;
    
    @Override
    public String sendVerificationCode(String phoneNumber) {
        // 6자리 랜덤 인증 코드 생성
        String verificationCode = generateRandomCode();
        
        // 실제 SMS 발송 로직은 여기에 구현 (외부 SMS API 사용)
        // smsService.sendSms(phoneNumber, "인증번호: " + verificationCode);
        
        // 인증 코드를 Redis에 저장 (5분 유효)
        redisTemplate.opsForValue().set(
                "PHONE:" + phoneNumber,
                verificationCode,
                5,
                TimeUnit.MINUTES
        );
        
        return verificationCode;
    }
    
    @Override
    public boolean verifyCode(String phoneNumber, String code) {
        String savedCode = redisTemplate.opsForValue().get("PHONE:" + phoneNumber);
        if (savedCode != null && savedCode.equals(code)) {
            // 인증 성공 시 Redis에서 코드 삭제
            redisTemplate.delete("PHONE:" + phoneNumber);
            return true;
        }
        return false;
    }
    
    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 100000 ~ 999999
        return String.valueOf(code);
    }
}