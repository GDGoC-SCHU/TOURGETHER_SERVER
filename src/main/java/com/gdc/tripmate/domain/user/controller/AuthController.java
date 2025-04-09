package com.gdc.tripmate.domain.user.controller;

import com.gdc.tripmate.domain.phone.service.PhoneVerificationService;
import com.gdc.tripmate.domain.user.dto.LogoutRequest;
import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import com.gdc.tripmate.domain.user.status.Status;
import com.gdc.tripmate.global.security.dto.RefreshTokenRequest;
import com.gdc.tripmate.global.security.dto.TokenResponse;
import com.gdc.tripmate.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;
	private final UserRepository userRepository;
	private final PhoneVerificationService phoneVerificationService;

	@PostMapping("/refresh")
	public ResponseEntity<TokenResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
		// refreshToken 유효성 검증
		if (!jwtTokenProvider.validateToken(request.getRefreshToken())) {
			throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
		}

		// 사용자 이메일로 Redis에서 저장된 refreshToken 조회
		String email = request.getEmail();
		String savedRefreshToken = redisTemplate.opsForValue().get("RT:" + email);

		// Redis에 저장된 토큰과 요청으로 받은 토큰이 일치하는지 확인
		if (savedRefreshToken == null || !savedRefreshToken.equals(request.getRefreshToken())) {
			throw new IllegalArgumentException("토큰이 일치하지 않습니다.");
		}

		// 사용자 조회
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + email));

		// 새로운 accessToken 발급
		String newAccessToken = jwtTokenProvider.createAccessToken(user.getEmail(),
				user.getRoles());

		return ResponseEntity.ok(TokenResponse.builder()
				.accessToken(newAccessToken)
				.refreshToken(request.getRefreshToken())
				.needPhoneVerification(user.getStatus() == Status.PENDING)
				.userId(user.getId())
				.build());
	}

	@PostMapping("/logout")
	public ResponseEntity<Void> logout(@RequestBody LogoutRequest request) {
		// Redis에서 refreshToken 삭제
		redisTemplate.delete("RT:" + request.getEmail());
		return ResponseEntity.ok().build();
	}

	// 휴대폰 인증 요청
	@PostMapping("/phone/request")
	public ResponseEntity<String> requestPhoneVerification(@RequestParam Long userId,
			@RequestParam String phoneNumber) {
		// 사용자 조회
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

		// 휴대폰 인증 코드 발송
		String verificationCode = phoneVerificationService.sendVerificationCode(phoneNumber);

		// 임시로 사용자 휴대폰 번호 저장
		user.setPhoneNumber(phoneNumber);
		userRepository.save(user);

		return ResponseEntity.ok("인증번호가 발송되었습니다.");
	}

	// 휴대폰 인증 확인
	@PostMapping("/phone/verify")
	public ResponseEntity<String> verifyPhone(@RequestParam Long userId,
			@RequestParam String code) {
		// 사용자 조회
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + userId));

		// 인증 코드 확인
		boolean isVerified = phoneVerificationService.verifyCode(user.getPhoneNumber(), code);

		if (isVerified) {
			// 사용자 상태 변경
			user.setStatus(Status.ACTIVE);
			userRepository.save(user);
			return ResponseEntity.ok("휴대폰 인증이 완료되었습니다.");
		} else {
			return ResponseEntity.badRequest().body("인증번호가 일치하지 않습니다.");
		}
	}
}