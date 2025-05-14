// SessionService.java - 세션 관리 서비스
package com.gdc.tripmate.domain.user.service;

import com.gdc.tripmate.domain.user.dto.AuthResult;
import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import com.gdc.tripmate.global.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 세션 관리 관련 비즈니스 로직을 담당하는 서비스 서버 측 세션 관리의 핵심 로직 담당
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SessionService {

	private final RedisTemplate<String, String> redisTemplate;
	private final JwtTokenProvider jwtTokenProvider;
	private final UserRepository userRepository;
	private final CookieService cookieService;

	/**
	 * 세션 저장
	 */
	public void saveSession(String sessionId, String refreshToken, String email) {
		// 세션 ID와 리프레시 토큰을 Redis에 저장 (서버 측 관리)
		redisTemplate.opsForValue().set(
				"SESSION:" + sessionId,
				refreshToken,
				jwtTokenProvider.getRefreshTokenValidityInMilliseconds(),
				TimeUnit.MILLISECONDS
		);

		// 이메일과 세션 ID 매핑도 유지 (로그아웃 시 삭제용)
		redisTemplate.opsForValue().set(
				"RT:" + email,
				sessionId,
				jwtTokenProvider.getRefreshTokenValidityInMilliseconds(),
				TimeUnit.MILLISECONDS
		);
	}

	/**
	 * 세션 제거
	 */
	public void removeSession(String sessionId, String email) {
		redisTemplate.delete("SESSION:" + sessionId);
		redisTemplate.delete("RT:" + email);
	}

	/**
	 * 세션 리프레시 세션 ID로 리프레시 토큰을 찾아 검증하고 새 액세스 토큰 발급
	 */
	public AuthResult refreshSession(String sessionId, HttpServletResponse response) {
		try {
			// Redis에서 세션 ID로 리프레시 토큰 조회
			String refreshToken = redisTemplate.opsForValue().get("SESSION:" + sessionId);

			if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)) {
				return AuthResult.builder()
						.success(false)
						.build();
			}

			// 리프레시 토큰에서 사용자 정보 추출
			String email = jwtTokenProvider.getEmailFromToken(refreshToken);
			User user = userRepository.findByEmail(email)
					.orElseThrow(() -> new RuntimeException("User not found"));

			// 새 액세스 토큰 생성
			String newAccessToken = jwtTokenProvider.createAccessToken(email, user.getRoles());

			// 액세스 토큰을 HTTP-Only 쿠키로 설정
			cookieService.setAccessTokenCookie(response, newAccessToken);

			return AuthResult.builder()
					.success(true)
					.accessToken(newAccessToken)
					.email(email)
					.build();

		} catch (Exception e) {
			log.error("세션 리프레시 중 오류 발생", e);
			return AuthResult.builder()
					.success(false)
					.build();
		}
	}
}