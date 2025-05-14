package com.gdc.tripmate.domain.phone.service;

import com.gdc.tripmate.domain.phone.dto.SmsVerificationResponse;
import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import com.gdc.tripmate.domain.user.status.Status;
import com.gdc.tripmate.global.security.dto.TokenResponse;
import com.gdc.tripmate.global.security.jwt.JwtTokenProvider;
import com.gdc.tripmate.global.security.jwt.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsVerificationService {

	private final RedisTemplate<String, String> redisTemplate;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final SecurityUtils securityUtils;
	private final FirebasePhoneAuthService firebasePhoneAuthService;
	private final SecureRandom random = new SecureRandom();

	// 인증 코드 유효 시간 (분)
	private static final int VERIFICATION_CODE_TTL = 3;

	/**
	 * 인증 코드 생성 및 저장
	 * 프론트엔드가 JWT 토큰을 보내므로 토큰에서 사용자 정보를 추출하도록 수정
	 */
	public SmsVerificationResponse generateVerificationCode(String phoneNumber,
			Long providedUserId) {
		// JWT 토큰에서 사용자 ID 추출
		final Long currentUserId = securityUtils.getCurrentUserId();
		// 만약 currentUserId가 null이고 providedUserId가 제공된 경우, providedUserId 사용
		final Long finalUserId =
				(currentUserId == null && providedUserId != null) ? providedUserId : currentUserId;

		log.info("인증 코드 생성: phoneNumber={}, userId={}", phoneNumber, finalUserId);

		// 사용자 확인 (userId가 있는 경우)
		if (finalUserId != null) {
			userRepository.findById(finalUserId)
					.orElseThrow(
							() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + finalUserId));
		}

		// 전화번호 형식 포맷팅 (프론트엔드와 동일한 로직)
		String formattedPhone = formatPhoneNumber(phoneNumber);

		try {
			// Firebase 전화번호 인증 서비스를 사용하여 인증 코드 발송
			firebasePhoneAuthService.sendVerificationCode(formattedPhone);

			// Firebase는 실제 인증 코드를 공개하지 않으므로, 개발 환경에서는 Redis에서 코드 가져오기
			String verificationCode = redisTemplate.opsForValue()
					.get("PHONE_AUTH:" + formattedPhone);

			return SmsVerificationResponse.builder()
					.success(true)
					.code(verificationCode) // 개발용으로만 코드 포함 (실제 운영에서는 제거)
					.message("인증 코드가 발송되었습니다.")
					.expiresInSeconds(VERIFICATION_CODE_TTL * 60)
					.build();
		} catch (Exception e) {
			log.error("인증 코드 발송 실패", e);
			return SmsVerificationResponse.builder()
					.success(false)
					.message("인증 코드 발송에 실패했습니다: " + e.getMessage())
					.build();
		}
	}

	/**
	 * 인증 코드 검증
	 * 프론트엔드가 JWT 토큰을 보내므로 토큰에서 사용자 정보를 추출하도록 수정
	 */
	public TokenResponse verifyCode(Long providedUserId, String phoneNumber, String code) {
		// JWT 토큰에서 사용자 ID 추출
		final Long currentUserId = securityUtils.getCurrentUserId();
		// 만약 currentUserId가 null이고 providedUserId가 제공된 경우, providedUserId 사용
		final Long finalUserId =
				(currentUserId == null && providedUserId != null) ? providedUserId : currentUserId;

		log.info("인증 코드 검증: userId={}, phoneNumber={}", finalUserId, phoneNumber);

		// 전화번호 형식 포맷팅
		String formattedPhone = formatPhoneNumber(phoneNumber);

		// Firebase 전화번호 인증 서비스를 사용하여 인증 코드 확인
		boolean isVerified = firebasePhoneAuthService.verifyCode(formattedPhone, code);

		if (!isVerified) {
			throw new IllegalArgumentException("인증 코드가 일치하지 않거나 만료되었습니다.");
		}

		// 사용자 조회 (userId가 있는 경우)
		User user = null;
		if (finalUserId != null) {
			user = userRepository.findById(finalUserId)
					.orElseThrow(
							() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + finalUserId));

			// 사용자 정보 업데이트
			user.setPhoneNumber(formattedPhone);
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
		} else {
			// 사용자 ID가 없는 경우 (프론트엔드에서는 항상 userId가 있어야 함)
			return TokenResponse.builder()
					.needPhoneVerification(false)
					.build();
		}
	}

	/**
	 * 전화번호 인증 상태 업데이트
	 * 컨트롤러에서 서비스 계층으로 이동된 로직
	 * @param userId 사용자 ID
	 * @param phoneNumber 전화번호
	 * @return 업데이트 성공 여부
	 */
	@Transactional
	public boolean updatePhoneVerificationStatus(Long userId, String phoneNumber) {
		if (userId == null) {
			throw new IllegalArgumentException("사용자 ID가 필요합니다.");
		}

		if (phoneNumber == null || phoneNumber.isEmpty()) {
			throw new IllegalArgumentException("전화번호가 필요합니다.");
		}

		// 전화번호 형식 포맷팅
		String formattedPhone = formatPhoneNumber(phoneNumber);

		// 사용자 조회
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

		// 사용자 정보 업데이트
		user.setPhoneNumber(formattedPhone);
		user.setStatus(Status.ACTIVE);
		userRepository.save(user);

		log.info("전화번호 인증 상태 업데이트 완료: userId={}, phoneNumber={}", userId, formattedPhone);
		return true;
	}

	/**
	 * 전화번호 형식 포맷팅 (프론트엔드와 동일한 로직)
	 */
	private String formatPhoneNumber(String input) {
		String numbers = input.replaceAll("[^0-9]", "");
		if (numbers.startsWith("0")) {
			return "+82" + numbers.substring(1);
		}

		if (input.startsWith("+")) {
			return input;
		}

		return "+82" + numbers;
	}
}