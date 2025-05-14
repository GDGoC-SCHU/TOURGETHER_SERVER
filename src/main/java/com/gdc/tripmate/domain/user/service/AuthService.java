package com.gdc.tripmate.domain.user.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * 인증 관련 비즈니스 로직을 정의하는 인터페이스
 * OAuth2 인증만 지원
 */
public interface AuthService {

	/**
	 * 인증 상태를 확인하고 필요시 액세스 토큰 갱신
	 *
	 * @param request HTTP 요청
	 * @param response HTTP 응답
	 * @return 인증 상태 정보
	 */
	Map<String, Object> checkAndRefreshAuthStatus(HttpServletRequest request, HttpServletResponse response);

	/**
	 * OAuth2 안내 메시지 반환
	 * 이 애플리케이션은 OAuth2 인증만 지원
	 *
	 * @return OAuth2 안내 메시지
	 */
	Map<String, Object> getOAuth2Guidance();

	/**
	 * 로그아웃 처리
	 *
	 * @param request HTTP 요청
	 * @param response HTTP 응답
	 */
	void logout(HttpServletRequest request, HttpServletResponse response);

	/**
	 * 토큰 리프레시
	 *
	 * @param request HTTP 요청
	 * @param response HTTP 응답
	 * @return 리프레시 결과
	 */
	Map<String, Object> refreshToken(HttpServletRequest request, HttpServletResponse response);
}