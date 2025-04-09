package com.gdc.tripmate.global.security.oauth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import com.gdc.tripmate.domain.user.status.Status;
import com.gdc.tripmate.global.security.dto.TokenResponse;
import com.gdc.tripmate.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

	private final JwtTokenProvider jwtTokenProvider;
	private final RedisTemplate<String, String> redisTemplate;
	private final UserRepository userRepository;
	private final ObjectMapper objectMapper;

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

		// OAuth 제공자 정보 가져오기
		String provider = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();

		// 이메일 추출
		String email = extractEmail(oAuth2User, provider);

		// 해당 이메일로 사용자 조회
		User user = userRepository.findByEmail(email)
				.orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자가 없습니다: " + email));

		// JWT 토큰 발급
		String accessToken = jwtTokenProvider.createAccessToken(user.getEmail(), user.getRoles());
		String refreshToken = jwtTokenProvider.createRefreshToken();

		// refreshToken을 Redis에 저장
		redisTemplate.opsForValue().set(
				"RT:" + user.getEmail(),
				refreshToken,
				jwtTokenProvider.getRefreshTokenValidityInMilliseconds(),
				TimeUnit.MILLISECONDS
		);

		// 사용자 상태를 확인하여 필요한 추가 정보 설정
		boolean needPhoneVerification = user.getStatus() == Status.PENDING;

		// 응답 설정
		response.setContentType("application/json;charset=UTF-8");

		TokenResponse tokenResponse = TokenResponse.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.needPhoneVerification(needPhoneVerification)
				.userId(user.getId())
				.build();

		response.getWriter().write(objectMapper.writeValueAsString(tokenResponse));
	}

	private String extractEmail(OAuth2User oAuth2User, String provider) {
		Map<String, Object> attributes = oAuth2User.getAttributes();

		if ("google".equalsIgnoreCase(provider)) {
			return (String) attributes.get("email");
		} else if ("kakao".equalsIgnoreCase(provider)) {
			Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get(
					"kakao_account");
			return (String) kakaoAccount.get("email");
		}

		throw new IllegalArgumentException("Unsupported provider: " + provider);
	}
}