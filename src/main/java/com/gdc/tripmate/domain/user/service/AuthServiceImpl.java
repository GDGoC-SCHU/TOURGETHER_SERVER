package com.gdc.tripmate.domain.user.service;

import com.gdc.tripmate.domain.user.dto.AuthResult;
import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import com.gdc.tripmate.domain.user.status.Status;
import com.gdc.tripmate.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 인증 관련 비즈니스 로직을 처리하는 서비스 구현체 OAuth2 인증만 지원
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final RedisTemplate<String, String> redisTemplate;
	private final CookieService cookieService;
	private final SessionService sessionService;

	/**
	 * 인증 상태를 확인하고 필요시 토큰을 갱신하는 메서드
	 */
	@Override
	@Transactional(readOnly = true)
	public Map<String, Object> checkAndRefreshAuthStatus(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			// 액세스 토큰 추출
			String accessToken = cookieService.extractCookieValue(request, "access_token");

			// 액세스 토큰이 없는 경우
			if (accessToken == null) {
				return createUnauthenticatedResponse();
			}

			// 액세스 토큰 검증
			if (!jwtTokenProvider.validateToken(accessToken)) {
				// 액세스 토큰이 만료된 경우, 세션 ID로 리프레시 시도
				String sessionId = cookieService.extractCookieValue(request, "session_id");
				if (sessionId == null) {
					return createUnauthenticatedResponse();
				}

				// 세션 서비스를 통해 리프레시 토큰 검증 및 새 액세스 토큰 발급
				AuthResult refreshResult = sessionService.refreshSession(sessionId, response);
				if (!refreshResult.isSuccess()) {
					cookieService.clearAuthCookies(response);
					return createUnauthenticatedResponse();
				}

				// 새 액세스 토큰 사용
				accessToken = refreshResult.getAccessToken();
			}

			// 토큰에서 사용자 정보 추출
			String email = jwtTokenProvider.getEmailFromToken(accessToken);
			User user = userRepository.findByEmail(email)
					.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

			// 사용자 인증 상태 반환
			boolean needPhoneVerification = user.getPhoneNumber() == null ||
					user.getPhoneNumber().isEmpty() ||
					user.getStatus() == Status.PENDING;

			return Map.of(
					"isAuthenticated", true,
					"needPhoneVerification", needPhoneVerification,
					"userId", user.getId()
			);

		} catch (Exception e) {
			log.error("인증 상태 확인 중 오류 발생", e);
			cookieService.clearAuthCookies(response);
			return createUnauthenticatedResponse();
		}
	}

	/**
	 * OAuth2 안내 메시지 반환
	 */
	@Override
	public Map<String, Object> getOAuth2Guidance() {
		// OAuth2 인증만 지원함을 알리는 응답 생성
		log.info("일반 로그인 시도 감지: 이 애플리케이션은 OAuth2 인증만 지원합니다");

		Map<String, Object> result = new HashMap<>();
		result.put("success", false);
		result.put("message", "이 애플리케이션은 소셜 로그인(OAuth2)만 지원합니다. 소셜 로그인 버튼을 이용해주세요.");
		result.put("authType", "oauth2");
		result.put("supportedProviders", new String[]{"google", "kakao", "naver"});

		return result;
	}

	/**
	 * 로그아웃 처리 메서드
	 */
	@Override
	@Transactional
	public void logout(HttpServletRequest request, HttpServletResponse response) {
		try {
			// 액세스 토큰에서 이메일 추출
			String accessToken = cookieService.extractCookieValue(request, "access_token");
			if (accessToken != null) {
				String email = jwtTokenProvider.getEmailFromToken(accessToken);

				// 세션 제거
				String sessionId = redisTemplate.opsForValue().get("RT:" + email);
				if (sessionId != null) {
					sessionService.removeSession(sessionId, email);
				}
			}

			// 인증 관련 쿠키 삭제
			cookieService.clearAuthCookies(response);

		} catch (Exception e) {
			log.error("로그아웃 중 오류 발생", e);
			// 쿠키는 항상 제거
			cookieService.clearAuthCookies(response);
		}
	}

	/**
	 * 토큰 리프레시 메서드
	 */
	@Override
	@Transactional
	public Map<String, Object> refreshToken(HttpServletRequest request,
			HttpServletResponse response) {
		try {
			String sessionId = cookieService.extractCookieValue(request, "session_id");

			if (sessionId == null) {
				return createUnauthenticatedResponse();
			}

			// 세션 서비스를 통해 리프레시 토큰 검증 및 새 액세스 토큰 발급
			AuthResult refreshResult = sessionService.refreshSession(sessionId, response);
			if (!refreshResult.isSuccess()) {
				cookieService.clearAuthCookies(response);
				return createUnauthenticatedResponse();
			}

			// 토큰에서 사용자 정보 추출
			String email = jwtTokenProvider.getEmailFromToken(refreshResult.getAccessToken());
			User user = userRepository.findByEmail(email)
					.orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + email));

			// 사용자 인증 상태 반환
			boolean needPhoneVerification = user.getPhoneNumber() == null ||
					user.getPhoneNumber().isEmpty() ||
					user.getStatus() == Status.PENDING;

			return Map.of(
					"isAuthenticated", true,
					"needPhoneVerification", needPhoneVerification,
					"userId", user.getId()
			);

		} catch (Exception e) {
			log.error("토큰 리프레시 중 오류 발생", e);
			cookieService.clearAuthCookies(response);
			return createUnauthenticatedResponse();
		}
	}

	/**
	 * 인증되지 않은 상태의 응답 생성
	 */
	private Map<String, Object> createUnauthenticatedResponse() {
		Map<String, Object> response = new HashMap<>();
		response.put("isAuthenticated", false);
		response.put("needPhoneVerification", false);
		response.put("userId", null); // HashMap은 null 값을 허용합니다
		return response;
	}

}