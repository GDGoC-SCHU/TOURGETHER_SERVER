package com.gdc.tripmate.domain.phone.service;

import com.gdc.tripmate.global.firebase.FirebaseConfig;
import com.google.firebase.auth.FirebaseAuth;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebasePhoneAuthService {

	private final FirebaseConfig firebaseConfig;
	private final FirebaseAuth firebaseAuth;
	private final RedisTemplate<String, String> redisTemplate;

	// 인증 코드 유효 시간 (분)
	private static final int VERIFICATION_CODE_TTL = 3;

	/**
	 * 전화번호 인증 코드 발송
	 *
	 * @param phoneNumber 전화번호
	 * @return 세션 ID
	 */
	public String sendVerificationCode(String phoneNumber) {
		try {
			log.info("전화번호 인증 코드 발송: {}", phoneNumber);

			// Firebase를 통해 인증 코드 발송 (실제로는 Firebase Admin SDK가 직접 SMS 발송 기능 제공 X)
			// 따라서 Firebase Auth 세션만 생성하고 실제 SMS는 별도 서비스로 발송해야 함
			String sessionId = firebaseConfig.createPhoneAuthSession(phoneNumber);

			// 테스트를 위한 임시 코드 생성 (실제 환경에서는 Firebase에서 제공)
			String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));

			// Redis에 인증 코드 저장
			String redisKey = "PHONE_AUTH:" + phoneNumber;
			redisTemplate.opsForValue()
					.set(redisKey, verificationCode, VERIFICATION_CODE_TTL, TimeUnit.MINUTES);

			log.debug("생성된 인증 코드: {}", verificationCode);

			// 여기서 실제 SMS 발송 서비스 연동 필요
			// sendSms(phoneNumber, "인증 코드: " + verificationCode);

			return sessionId;
		} catch (Exception e) {
			log.error("전화번호 인증 코드 발송 실패", e);
			throw new RuntimeException("전화번호 인증 코드 발송에 실패했습니다.", e);
		}
	}

	/**
	 * 전화번호 인증 코드 확인
	 *
	 * @param phoneNumber 전화번호
	 * @param code        인증 코드
	 * @return 인증 성공 여부
	 */
	public boolean verifyCode(String phoneNumber, String code) {
		try {
			log.info("전화번호 인증 코드 확인: {}", phoneNumber);

			// Redis에서 저장된 코드 조회
			String redisKey = "PHONE_AUTH:" + phoneNumber;
			String storedCode = redisTemplate.opsForValue().get(redisKey);

			// 코드 검증
			if (storedCode == null) {
				log.warn("저장된 인증 코드 없음: {}", phoneNumber);
				return false;
			}

			boolean matches = storedCode.equals(code);

			if (matches) {
				// 인증 성공 시 Redis에서 코드 삭제
				redisTemplate.delete(redisKey);
				log.info("전화번호 인증 성공: {}", phoneNumber);
			} else {
				log.warn("인증 코드 불일치: 입력={}, 저장={}", code, storedCode);
			}

			return matches;
		} catch (Exception e) {
			log.error("전화번호 인증 코드 확인 실패", e);
			return false;
		}
	}
}