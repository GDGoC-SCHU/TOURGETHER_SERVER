package com.gdc.tripmate.domain.phone.service;

import com.gdc.tripmate.domain.phone.dto.response.SmsVerificationResponse;
import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import com.gdc.tripmate.domain.user.status.Status;
import com.gdc.tripmate.global.security.dto.TokenResponse;
import com.gdc.tripmate.global.security.jwt.JwtTokenProvider;
import com.gdc.tripmate.global.security.jwt.SecurityUtils;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SmsVerificationService {

	private final RedisTemplate<String, String> redisTemplate;
	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final SecurityUtils securityUtils;
	private final SecureRandom random = new SecureRandom();

	// 인증 코드 유효 시간 (분)
	private static final int VERIFICATION_CODE_TTL = 3;
	// 인증 코드 재발송 제한 시간 (초)
	private static final int VERIFICATION_ATTEMPT_LIMIT_SECONDS = 60;
	// 인증 코드 Redis 키 접두사
	private static final String PHONE_AUTH_KEY_PREFIX = "PHONE_AUTH:";
	// 재발송 제한 Redis 키 접두사
	private static final String VERIFICATION_ATTEMPT_KEY_PREFIX = "VERIFICATION_ATTEMPT:";

	/**
	 * 인증 코드 생성 및 발송 (실제로는 발송하지 않고 개발용 코드만 생성)
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

		// 전화번호 형식 포맷팅
		String formattedPhone = formatPhoneNumber(phoneNumber);

		try {
			// 재발송 제한 확인
			String rateLimitKey = VERIFICATION_ATTEMPT_KEY_PREFIX + formattedPhone;
			Boolean isRateLimited = redisTemplate.hasKey(rateLimitKey);

			if (Boolean.TRUE.equals(isRateLimited)) {
				Long ttl = redisTemplate.getExpire(rateLimitKey, TimeUnit.SECONDS);
				return SmsVerificationResponse.builder()
						.success(false)
						.message("인증 코드 발송 제한 중입니다. " + ttl + "초 후에 다시 시도해주세요.")
						.expiresInSeconds(ttl.intValue())
						.build();
			}

			// 인증 코드 생성 (6자리 숫자)
			String verificationCode = generateSixDigitCode();

			// Redis에 인증 코드 저장
			String redisKey = PHONE_AUTH_KEY_PREFIX + formattedPhone;
			redisTemplate.opsForValue().set(
					redisKey,
					verificationCode,
					VERIFICATION_CODE_TTL,
					TimeUnit.MINUTES
			);

			// 재발송 제한 설정
			redisTemplate.opsForValue().set(
					rateLimitKey,
					"true",
					VERIFICATION_ATTEMPT_LIMIT_SECONDS,
					TimeUnit.SECONDS
			);

			// 실제 SMS 발송 대신 로그로 코드 확인 (개발 환경)
			log.info("개발 모드: 인증 코드 생성 완료 = {}", verificationCode);

			return SmsVerificationResponse.builder()
					.success(true)
					.message("인증 코드가 발송되었습니다.")
					.code(verificationCode) // 개발 모드에서만 코드 노출
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

		try {
			// Redis에서 저장된 코드 조회
			String redisKey = PHONE_AUTH_KEY_PREFIX + formattedPhone;
			String storedCode = redisTemplate.opsForValue().get(redisKey);

			// 코드 검증
			if (storedCode == null) {
				throw new IllegalArgumentException("인증 코드가 만료되었습니다.");
			}

			if (!storedCode.equals(code)) {
				throw new IllegalArgumentException("인증 코드가 일치하지 않습니다.");
			}

			// 인증 성공 시 Redis에서 코드 삭제
			redisTemplate.delete(redisKey);

			// 재발송 제한 키 삭제
			String rateLimitKey = VERIFICATION_ATTEMPT_KEY_PREFIX + formattedPhone;
			redisTemplate.delete(rateLimitKey);

			// 사용자 조회 (userId가 있는 경우)
			User user = null;
			if (finalUserId != null) {
				user = userRepository.findById(finalUserId)
						.orElseThrow(
								() -> new IllegalArgumentException(
										"사용자를 찾을 수 없습니다: " + finalUserId));

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
		} catch (Exception e) {
			log.error("인증 코드 검증 실패", e);
			throw new IllegalArgumentException("인증 코드 검증에 실패했습니다: " + e.getMessage());
		}
	}

	/**
	 * 전화번호 인증 상태 업데이트
	 *
	 * @param userId      사용자 ID
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

	/**
	 * 6자리 랜덤 인증 코드 생성
	 */
	private String generateSixDigitCode() {
		return String.format("%06d", random.nextInt(1000000));
	}
}