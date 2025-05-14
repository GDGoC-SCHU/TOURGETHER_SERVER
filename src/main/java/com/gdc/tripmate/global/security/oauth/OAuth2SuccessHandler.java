// OAuth2SuccessHandler.java - 리펙토링 버전 (계속)
package com.gdc.tripmate.global.security.oauth;

import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import com.gdc.tripmate.domain.user.service.CookieService;
import com.gdc.tripmate.domain.user.service.SessionService;
import com.gdc.tripmate.domain.user.status.Status;
import com.gdc.tripmate.global.security.dto.TokenResponse;
import com.gdc.tripmate.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * OAuth2 로그인 성공 처리기 서비스 계층을 활용하여 책임 분리
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final SessionService sessionService;
	private final CookieService cookieService;

	// 프론트엔드 URL 설정
	@Value("${app.frontend.url:http://localhost:8081}")
	private String frontendUrl;

	// 모바일 앱 스킴 설정
	@Value("${app.mobile.scheme:tourgether}")
	private String mobileScheme;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		// OAuth 제공자 정보 가져오기
		String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

		log.info("OAuth2 로그인 성공: provider={}", provider);

		try {
			// 이메일 추출
			String email = extractEmail(oAuth2User, provider);
			log.info("OAuth2 사용자 이메일: {}", email);

			// 해당 이메일로 사용자 조회
			User user = userRepository.findByEmail(email)
					.orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 없습니다: " + email));

			// 전화번호 인증이 필요한지 확인
			boolean needPhoneVerification = user.getPhoneNumber() == null ||
					user.getPhoneNumber().isEmpty() ||
					user.getStatus() == Status.PENDING;

			// 웹 환경 확인
			boolean isWebEnvironment = isWebRequest(request);
			log.info("환경 감지: {}", isWebEnvironment ? "웹" : "앱");

			if (isWebEnvironment) {
				// 웹 환경: 세션 기반 인증 처리
				handleWebAuthentication(user, needPhoneVerification, response);
			} else {
				// 앱 환경: 기존 방식 유지 (토큰 파라미터 전달)
				handleAppAuthentication(user, needPhoneVerification, response);
			}
		} catch (Exception e) {
			log.error("OAuth2 로그인 처리 중 오류 발생", e);
			handleAuthenticationError(e, response);
		}
	}

	/**
	 * 웹 환경에서의 인증 처리
	 */
	private void handleWebAuthentication(User user, boolean needPhoneVerification,
			HttpServletResponse response) throws IOException {
		// 세션 ID 생성
		String sessionId = UUID.randomUUID().toString();

		// 액세스 토큰 및 리프레시 토큰 생성
		String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRoles());
		String refreshToken = jwtTokenProvider.createRefreshToken();

		// 세션 서비스를 통해 세션 저장
		sessionService.saveSession(sessionId, refreshToken, user.getEmail());

		// 쿠키 서비스를 통해 쿠키 설정
		cookieService.setAccessTokenCookie(response, accessToken);
		cookieService.setSessionIdCookie(response, sessionId);

		// 소셜 로그인 콜백 페이지로 리다이렉트 (쿠키는 자동으로 전송됨)
		String callbackUrl = UriComponentsBuilder.fromUriString(frontendUrl)
				.path("/auth/socialCallBack")
				.queryParam("userId", user.getId())
				.queryParam("needPhoneVerification", needPhoneVerification)
				.build()
				.toUriString();

		log.info("웹 환경 리다이렉트: {}", callbackUrl);
		response.sendRedirect(callbackUrl);
	}

	/**
	 * 앱 환경에서의 인증 처리
	 */
	private void handleAppAuthentication(User user, boolean needPhoneVerification,
			HttpServletResponse response) throws IOException {
		// 액세스 토큰 및 리프레시 토큰 생성
		String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRoles());
		String refreshToken = jwtTokenProvider.createRefreshToken();

		// Redis에 리프레시 토큰 저장 (앱은 세션 방식이 아니라 토큰 방식 유지)
		sessionService.saveSession(refreshToken, refreshToken, user.getEmail());

		// 토큰 응답 생성 (앱용)
		TokenResponse tokenResponse = TokenResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.needPhoneVerification(needPhoneVerification)
				.userId(user.getId())
				.build();

		// 앱 환경: 딥링크 리다이렉트
		String appRedirectUri = buildRedirectUriForApp(tokenResponse);
		log.info("앱 환경 리다이렉트: {}", appRedirectUri);
		response.sendRedirect(appRedirectUri);
	}

	/**
	 * 인증 에러 처리
	 */
	private void handleAuthenticationError(Exception e, HttpServletResponse response)
			throws IOException {
		// 에러 발생 시 에러 페이지로 리다이렉트
		String errorRedirectUrl = UriComponentsBuilder.fromUriString(frontendUrl)
				.path("/auth/error")
				.queryParam("message", e.getMessage())
				.build()
				.toUriString();

		response.sendRedirect(errorRedirectUrl);
	}

	/**
	 * 웹 요청인지 확인
	 */
	private boolean isWebRequest(HttpServletRequest request) {
		// web=true 파라미터 확인
		String webParam = request.getParameter("web");
		if ("true".equals(webParam)) {
			return true;
		}

		// User-Agent 확인
		String userAgent = request.getHeader("User-Agent");
		boolean isBrowser = userAgent != null && (
				userAgent.contains("Mozilla") ||
						userAgent.contains("Chrome") ||
						userAgent.contains("Safari") ||
						userAgent.contains("Firefox")
		);

		// Expo Go 앱인지 확인
		boolean isExpoApp = userAgent != null && userAgent.contains("Expo");

		return isBrowser && !isExpoApp;
	}

	/**
	 * 앱용 리다이렉트 URI 생성
	 */
	private String buildRedirectUriForApp(TokenResponse tokenResponse) {
		String scheme = mobileScheme + "://";
		String path = tokenResponse.isNeedPhoneVerification() ?
				"auth/VerifyPhone" : "auth-callback";

		return UriComponentsBuilder.fromUriString(scheme + path)
				.queryParam("accessToken", tokenResponse.getAccessToken())
				.queryParam("refreshToken", tokenResponse.getRefreshToken())
				.queryParam("userId", tokenResponse.getUserId())
				.queryParam("needPhoneVerification", tokenResponse.isNeedPhoneVerification())
				.build()
				.toUriString();
	}

	/**
	 * 이메일 추출
	 */
	private String extractEmail(OAuth2User oAuth2User, String provider) {
		Map<String, Object> attributes = oAuth2User.getAttributes();

		if ("google".equalsIgnoreCase(provider)) {
			return (String) attributes.get("email");
		} else if ("kakao".equalsIgnoreCase(provider)) {
			Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get(
					"kakao_account");
			return (String) kakaoAccount.get("email");
		} else if ("naver".equalsIgnoreCase(provider)) {
			Map<String, Object> response = (Map<String, Object>) attributes.get("response");
			return (String) response.get("email");
		}

		throw new IllegalArgumentException("Unsupported provider: " + provider);
	}
}