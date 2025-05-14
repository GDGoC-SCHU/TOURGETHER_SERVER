// JwtAuthenticationFilter.java - 리팩토링 버전
package com.gdc.tripmate.global.security.jwt;

import com.gdc.tripmate.domain.user.dto.AuthResult;
import com.gdc.tripmate.domain.user.service.CookieService;
import com.gdc.tripmate.domain.user.service.SessionService;
import com.gdc.tripmate.global.security.customUser.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * JWT 인증 필터 요청마다 JWT 토큰을 검증하고 인증 처리
 */
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private final JwtTokenProvider jwtTokenProvider;
	private final CustomUserDetailsService userDetailsService;
	private final SessionService sessionService;
	private final CookieService cookieService;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain filterChain) throws ServletException, IOException {
		try {
			// 토큰 추출 (헤더 또는 쿠키)
			String token = extractToken(request);

			if (token != null) {
				processToken(token, request, response);
			}
		} catch (Exception e) {
			log.error("JWT 인증 처리 중 오류 발생: {}", e.getMessage());
		}

		filterChain.doFilter(request, response);
	}

	/**
	 * 토큰 추출 (헤더 또는 쿠키)
	 */
	private String extractToken(HttpServletRequest request) {
		// 1. 헤더에서 토큰 추출 시도
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}

		// 2. 쿠키에서 액세스 토큰 추출 시도
		return cookieService.extractCookieValue(request, "access_token");
	}

	/**
	 * 토큰 처리 (검증 및 인증)
	 */
	private void processToken(String token, HttpServletRequest request,
			HttpServletResponse response) {
		// 토큰 유효성 검증
		if (jwtTokenProvider.validateToken(token)) {
			// 유효한 토큰은 인증 처리
			authenticateUser(token, request);
		} else {
			// 유효하지 않은 토큰은 리프레시 시도
			refreshToken(request, response);
		}
	}

	/**
	 * 사용자 인증 처리
	 */
	private void authenticateUser(String token, HttpServletRequest request) {
		try {
			String email = jwtTokenProvider.getEmailFromToken(token);
			UserDetails userDetails = userDetailsService.loadUserByUsername(email);

			UsernamePasswordAuthenticationToken authentication =
					new UsernamePasswordAuthenticationToken(userDetails, null,
							userDetails.getAuthorities());

			authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
			SecurityContextHolder.getContext().setAuthentication(authentication);
		} catch (Exception e) {
			log.error("사용자 인증 처리 중 오류 발생: {}", e.getMessage());
		}
	}

	/**
	 * 토큰 리프레시 시도
	 */
	private void refreshToken(HttpServletRequest request, HttpServletResponse response) {
		try {
			// 세션 ID 쿠키에서 값 추출
			String sessionId = cookieService.extractCookieValue(request, "session_id");

			if (sessionId != null) {
				// 세션 서비스를 통해 토큰 리프레시 시도
				AuthResult refreshResult = sessionService.refreshSession(sessionId, response);

				if (refreshResult.isSuccess()) {
					// 리프레시 성공 시 새 액세스 토큰으로 인증 처리
					authenticateUser(refreshResult.getAccessToken(), request);
				}
			}
		} catch (Exception e) {
			log.error("토큰 리프레시 중 오류 발생: {}", e.getMessage());
		}
	}
}