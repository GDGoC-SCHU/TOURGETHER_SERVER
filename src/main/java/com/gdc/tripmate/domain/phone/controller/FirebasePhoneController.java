package com.gdc.tripmate.domain.phone.controller;


import com.gdc.tripmate.domain.phone.dto.FirebaseAuthRequest;
import com.gdc.tripmate.domain.phone.dto.FirebasePhoneRequest;
import com.gdc.tripmate.domain.phone.service.FirebasePhoneAuthService;
import com.gdc.tripmate.global.security.dto.TokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/firebase")
@RequiredArgsConstructor
@Slf4j
public class FirebasePhoneController {

	private final FirebasePhoneAuthService firebaseAuthService;

	/**
	 * Firebase ID 토큰으로 전화번호 인증 처리
	 */
	@PostMapping("/verify-phone-token")
	public ResponseEntity<TokenResponse> verifyPhoneWithIdToken(
			@RequestBody FirebaseAuthRequest request,
			@RequestParam Long userId) {
		log.info("Firebase ID 토큰으로 전화번호 인증 요청: userId={}", userId);
		TokenResponse response = firebaseAuthService.verifyPhoneWithIdToken(request.getIdToken(),
				userId);
		return ResponseEntity.ok(response);
	}

	/**
	 * 백엔드에서 전화번호 인증 처리 (참고용: 실제로는 클라이언트에서 인증 필요)
	 */
	@PostMapping("/verify-phone")
	public ResponseEntity<TokenResponse> verifyPhoneNumber(
			@RequestBody FirebasePhoneRequest request) {
		log.info("전화번호 인증 요청: userId={}, phoneNumber={}", request.getUserId(),
				request.getPhoneNumber());
		TokenResponse response = firebaseAuthService.verifyPhoneNumber(request);
		return ResponseEntity.ok(response);
	}
}