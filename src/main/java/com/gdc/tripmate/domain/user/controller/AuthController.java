package com.gdc.tripmate.domain.user.controller;

import com.gdc.tripmate.domain.user.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 인증 관련 API를 처리하는 컨트롤러 API 흐름을 명확히 보여주기 위해 모든 엔드포인트를 유지하지만, 내부적으로는 OAuth2 인증만 사용
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

	private final AuthService authService;

	/**
	 * 인증 상태 확인 API
	 */
	@GetMapping("/status")
	public ResponseEntity<Map<String, Object>> checkAuthStatus(HttpServletRequest request,
			HttpServletResponse response) {
		// 서비스 계층에 위임
		Map<String, Object> authStatus = authService.checkAndRefreshAuthStatus(request, response);
		return ResponseEntity.ok(authStatus);
	}

	/**
	 * 로그인 API 참고: 이 애플리케이션은 OAuth2 인증만 지원합니다.
	 */
	@PostMapping("/login")
	public ResponseEntity<Map<String, Object>> login() {
		// OAuth2 안내 메시지 반환
		return ResponseEntity.ok(authService.getOAuth2Guidance());
	}

	/**
	 * 로그아웃 API
	 */
	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(HttpServletRequest request,
			HttpServletResponse response) {
		// 서비스 계층에 위임
		authService.logout(request, response);
		return ResponseEntity.ok(Map.of("message", "로그아웃 성공"));
	}

	/**
	 * 리프레시 토큰으로 액세스 토큰 갱신 API
	 */
	@PostMapping("/refresh")
	public ResponseEntity<Map<String, Object>> refreshToken(HttpServletRequest request,
			HttpServletResponse response) {
		// 서비스 계층에 위임
		Map<String, Object> refreshResult = authService.refreshToken(request, response);
		return ResponseEntity.ok(refreshResult);
	}
}