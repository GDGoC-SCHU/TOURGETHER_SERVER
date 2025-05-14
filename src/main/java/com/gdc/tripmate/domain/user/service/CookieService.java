package com.gdc.tripmate.domain.user.service;

import com.gdc.tripmate.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 쿠키 관리 관련 비즈니스 로직을 담당하는 서비스
 * 단일 책임 원칙(SRP)에 따라 쿠키 관련 로직을 분리
 */
@Service
@RequiredArgsConstructor
public class CookieService {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 요청에서 쿠키 값 추출
     */
    public String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    /**
     * 액세스 토큰 쿠키 설정
     */
    public void setAccessTokenCookie(HttpServletResponse response, String accessToken) {
        Cookie cookie = createSecureCookie("access_token", accessToken);
        cookie.setMaxAge((int) jwtTokenProvider.getAccessTokenValidityInSeconds());
        response.addCookie(cookie);
    }

    /**
     * 세션 ID 쿠키 설정
     */
    public void setSessionIdCookie(HttpServletResponse response, String sessionId) {
        Cookie cookie = createSecureCookie("session_id", sessionId);
        cookie.setMaxAge((int) jwtTokenProvider.getRefreshTokenValidityInSeconds());
        response.addCookie(cookie);
    }

    /**
     * 인증 관련 쿠키 삭제
     */
    public void clearAuthCookies(HttpServletResponse response) {
        // 액세스 토큰 쿠키 삭제
        Cookie accessTokenCookie = new Cookie("access_token", null);
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        
        // 세션 ID 쿠키 삭제
        Cookie sessionIdCookie = new Cookie("session_id", null);
        sessionIdCookie.setHttpOnly(true);
        sessionIdCookie.setSecure(true);
        sessionIdCookie.setPath("/");
        sessionIdCookie.setMaxAge(0);
        
        response.addCookie(accessTokenCookie);
        response.addCookie(sessionIdCookie);
    }

    /**
     * 보안 쿠키 생성 헬퍼 메서드
     */
    private Cookie createSecureCookie(String name, String value) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true); // HTTPS 환경에서만 true로 설정
        cookie.setPath("/");
        cookie.setAttribute("SameSite", "Lax");
        return cookie;
    }
}