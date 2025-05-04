package com.gdc.tripmate.domain.phone.service;

import com.gdc.tripmate.domain.phone.dto.FirebasePhoneRequest;
import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import com.gdc.tripmate.domain.user.status.Status;
import com.gdc.tripmate.global.security.dto.TokenResponse;
import com.gdc.tripmate.global.security.jwt.JwtTokenProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.UserRecord;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class FirebasePhoneAuthService {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;

	/**
	 * Firebase Admin SDK를 사용한 전화번호 인증 확인 참고: 실제로 Firebase Phone Auth는 클라이언트 SDK에서 reCAPTCHA와 함께
	 * 사용하므로 백엔드에서만 완전히 처리하기는 어렵습니다.
	 */
	public TokenResponse verifyPhoneNumber(FirebasePhoneRequest request) {
		try {
			log.info("전화번호 인증: userId={}, phoneNumber={}", request.getUserId(),
					request.getPhoneNumber());

			// 사용자 조회
			User user = userRepository.findById(request.getUserId())
					.orElseThrow(() -> new IllegalArgumentException(
							"사용자를 찾을 수 없습니다: " + request.getUserId()));

			// Firebase에서 전화번호 인증 상태 확인
			// 참고: 실제 Firebase Admin SDK에는 이런 메서드가 없으므로 클라이언트 측에서 인증 필요
			// 이 부분은 개념적인 코드입니다
			boolean isVerified = verifyPhoneNumberWithFirebase(request.getPhoneNumber(),
					request.getVerificationId(), request.getVerificationCode());

			if (!isVerified) {
				throw new IllegalArgumentException("전화번호 인증에 실패했습니다.");
			}

			// 사용자 정보 업데이트
			user.setPhoneNumber(request.getPhoneNumber());
			user.setStatus(Status.ACTIVE);
			userRepository.save(user);

			// JWT 토큰 발급
			String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(),
					user.getRoles());
			String refreshToken = jwtTokenProvider.createRefreshToken();

			// refreshToken을 Redis에 저장
			redisTemplate.opsForValue().set(
					"RT:" + user.getEmail(),
					refreshToken,
					jwtTokenProvider.getRefreshTokenValidityInMilliseconds(),
					TimeUnit.MILLISECONDS
			);

			return TokenResponse.builder()
					.accessToken(accessToken)
					.refreshToken(refreshToken)
					.needPhoneVerification(false)
					.userId(user.getId())
					.build();

		} catch (Exception e) {
			log.error("전화번호 인증 실패", e);
			throw new RuntimeException("전화번호 인증 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * Firebase ID 토큰으로 인증된 사용자의 전화번호 확인
	 */
	public TokenResponse verifyPhoneWithIdToken(String idToken, Long userId) {
		try {
			log.info("Firebase ID 토큰으로 전화번호 인증: userId={}", userId);

			// Firebase ID 토큰 검증
			var decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
			String uid = decodedToken.getUid();

			// Firebase에서 사용자 정보 조회
			UserRecord userRecord = FirebaseAuth.getInstance().getUser(uid);
			String phoneNumber = userRecord.getPhoneNumber();

			if (phoneNumber == null || phoneNumber.isEmpty()) {
				throw new IllegalArgumentException("전화번호 정보가 없습니다.");
			}

			log.info("Firebase 사용자 전화번호: {}", phoneNumber);

			// 사용자 조회
			User user = userRepository.findById(userId)
					.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

			// 사용자 정보 업데이트
			user.setPhoneNumber(phoneNumber);
			user.setStatus(Status.ACTIVE);
			userRepository.save(user);

			// JWT 토큰 발급
			String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(),
					user.getRoles());
			String refreshToken = jwtTokenProvider.createRefreshToken();

			// refreshToken을 Redis에 저장
			redisTemplate.opsForValue().set(
					"RT:" + user.getEmail(),
					refreshToken,
					jwtTokenProvider.getRefreshTokenValidityInMilliseconds(),
					TimeUnit.MILLISECONDS
			);

			return TokenResponse.builder()
					.accessToken(accessToken)
					.refreshToken(refreshToken)
					.needPhoneVerification(false)
					.userId(user.getId())
					.build();

		} catch (FirebaseAuthException e) {
			log.error("Firebase ID 토큰 검증 실패", e);
			throw new RuntimeException("Firebase ID 토큰 검증 실패: " + e.getMessage(), e);
		}
	}

	/**
	 * 실제로는 Firebase Admin SDK에서 직접 지원하지 않음 클라이언트에서 Firebase SDK로 인증해야 함
	 */
	private boolean verifyPhoneNumberWithFirebase(String phoneNumber, String verificationId,
			String verificationCode) {
		// 이 메서드는 실제로 Firebase Admin SDK에서 제공하지 않습니다.
		// 실제 구현에서는 클라이언트에서 Firebase Auth SDK를 사용하여 인증해야 합니다.
		log.warn("Firebase Admin SDK는 직접적인 SMS 코드 검증을 지원하지 않습니다. 클라이언트에서 검증해야 합니다.");
		return true; // 개발용으로 항상 성공 반환 (실제 구현에서는 사용하지 않음)
	}
}