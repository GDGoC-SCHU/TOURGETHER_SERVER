package com.gdc.tripmate.domain.phone.controller;

import com.gdc.tripmate.domain.phone.dto.response.SmsVerificationResponse;
import com.gdc.tripmate.domain.phone.service.SmsVerificationService;
import com.gdc.tripmate.global.security.dto.TokenResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api")
public class SmsVerificationController {

	private final SmsVerificationService smsVerificationService;

	/**
	 * 인증 코드 발송 API (모바일 환경용) 프론트엔드의 /api/phone/sendVerification 경로 처리
	 */
	@PostMapping("/phone/sendVerification")
	public ResponseEntity<Map<String, Object>> sendVerification(
			@RequestBody Map<String, Object> request) {

		log.info("인증 코드 발송 요청: phoneNumber={}, userId={}",
				request.get("phoneNumber"), request.get("userId"));

		try {
			// userId가 있는 경우 Long으로 변환
			Long userId = null;
			if (request.get("userId") != null) {
				userId = Long.parseLong(request.get("userId").toString());
			}

			// 서비스 메서드 호출 (generateVerificationCode)
			SmsVerificationResponse response = smsVerificationService.generateVerificationCode(
					(String) request.get("phoneNumber"), userId);

			Map<String, Object> result = new HashMap<>();
			result.put("success", response.isSuccess());
			result.put("message", response.getMessage());

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			log.error("인증 코드 발송 실패", e);

			Map<String, Object> result = new HashMap<>();
			result.put("success", false);
			result.put("message", e.getMessage());

			return ResponseEntity.badRequest().body(result);
		}
	}

	/**
	 * 인증 코드 확인 API (모바일 환경용) 프론트엔드의 /api/phone/verifyCode 경로 처리
	 */
	@PostMapping("/phone/verifyCode")
	public ResponseEntity<Map<String, Object>> verifyCode(
			@RequestBody Map<String, Object> request) {

		log.info("인증 코드 확인 요청: phoneNumber={}, code={}, userId={}",
				request.get("phoneNumber"), request.get("code"), request.get("userId"));

		try {
			// userId가 있는 경우 Long으로 변환
			Long userId = null;
			if (request.get("userId") != null) {
				userId = Long.parseLong(request.get("userId").toString());
			}

			// 서비스 메서드 호출
			TokenResponse tokenResponse = smsVerificationService.verifyCode(
					userId, // providedUserId를 전달
					(String) request.get("phoneNumber"),
					(String) request.get("code"));

			Map<String, Object> result = new HashMap<>();
			result.put("success", true);
			result.put("message", "인증 코드 확인이 완료되었습니다.");

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			log.error("인증 코드 확인 실패", e);

			Map<String, Object> result = new HashMap<>();
			result.put("success", false);
			result.put("message", e.getMessage());

			return ResponseEntity.badRequest().body(result);
		}
	}

	/**
	 * 사용자 전화번호 인증 상태 업데이트 API 프론트엔드의 /api/users/{userId}/verifyPhone 경로 처리
	 */
	@PostMapping("/users/{userId}/verifyPhone")
	public ResponseEntity<Map<String, Object>> updatePhoneVerification(
			@PathVariable Long userId,
			@RequestBody Map<String, String> request) {

		log.info("전화번호 인증 상태 업데이트 요청: userId={}, phoneNumber={}",
				userId, request.get("phoneNumber"));

		try {
			// 서비스 메서드 호출 - 객체 수정 로직을 서비스로 이동
			boolean updated = smsVerificationService.updatePhoneVerificationStatus(
					userId, request.get("phoneNumber"));

			Map<String, Object> result = new HashMap<>();
			result.put("success", updated);
			result.put("message", "전화번호 인증 상태가 업데이트되었습니다.");

			return ResponseEntity.ok(result);
		} catch (Exception e) {
			log.error("전화번호 인증 상태 업데이트 실패", e);

			Map<String, Object> result = new HashMap<>();
			result.put("success", false);
			result.put("message", e.getMessage());

			return ResponseEntity.badRequest().body(result);
		}
	}
}