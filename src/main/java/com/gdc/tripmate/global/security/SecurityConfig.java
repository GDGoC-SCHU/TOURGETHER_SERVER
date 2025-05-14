// SecurityConfig.java - 리팩토링 버전
package com.gdc.tripmate.global.security;

import com.gdc.tripmate.domain.user.service.CookieService;
import com.gdc.tripmate.domain.user.service.CustomOAuth2UserService;
import com.gdc.tripmate.domain.user.service.SessionService;
import com.gdc.tripmate.global.security.customUser.CustomAuthenticationEntryPoint;
import com.gdc.tripmate.global.security.customUser.CustomUserDetailsService;
import com.gdc.tripmate.global.security.jwt.JwtAuthenticationFilter;
import com.gdc.tripmate.global.security.jwt.JwtTokenProvider;
import com.gdc.tripmate.global.security.oauth.OAuth2SuccessHandler;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Spring Security 설정 클래스
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final OAuth2SuccessHandler oAuth2SuccessHandler;
	private final CustomAuthenticationEntryPoint authenticationEntryPoint;
	private final JwtTokenProvider jwtTokenProvider;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final CustomUserDetailsService customUserDetailsService;
	private final SessionService sessionService;
	private final CookieService cookieService;

	/**
	 * 보안 필터 체인 설정
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				// CSRF 보호 설정
				.csrf(AbstractHttpConfigurer::disable)

				// CORS 설정
				.cors(cors -> cors
						.configurationSource(corsConfigurationSource()))

				// 세션 관리 설정 (무상태)
				.sessionManagement(session -> session
						.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				// 예외 처리 설정
				.exceptionHandling(exception -> exception
						.authenticationEntryPoint(authenticationEntryPoint))

				// URL 기반 권한 설정
				.authorizeHttpRequests(authorize -> authorize
						// 기존 허용 경로
						.requestMatchers("/api/auth/**", "/oauth2/**", "/login/**").permitAll()
						// 전화번호 인증 API 경로 허용 추가
						.requestMatchers("/api/phone/**", "/api/phone/sendVerification",
								"/api/phone/verifyCode").permitAll()
						// userId가 경로에 포함된 API도 허용
						.requestMatchers("/api/users/*/verifyPhone").authenticated()
						.requestMatchers("/api/admin/**").hasRole("ADMIN")
						.anyRequest().authenticated())

				// OAuth2 로그인 설정
				.oauth2Login(oauth2 -> oauth2
						.successHandler(oAuth2SuccessHandler)
						.userInfoEndpoint(userInfo -> userInfo
								.userService(customOAuth2UserService)));

		// JWT 인증 필터 추가
		http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	/**
	 * JWT 인증 필터 빈
	 */
	@Bean
	public JwtAuthenticationFilter jwtAuthenticationFilter() {
		return new JwtAuthenticationFilter(jwtTokenProvider, customUserDetailsService,
				sessionService, cookieService);
	}

	/**
	 * 인증 관리자 빈
	 */
	@Bean
	public AuthenticationManager authenticationManager(
			AuthenticationConfiguration authenticationConfiguration) throws Exception {
		return authenticationConfiguration.getAuthenticationManager();
	}

	/**
	 * CSRF 토큰 저장소 빈
	 */
	@Bean
	public CsrfTokenRepository csrfTokenRepository() {
		HttpSessionCsrfTokenRepository repository = new HttpSessionCsrfTokenRepository();
		repository.setHeaderName("X-CSRF-TOKEN");
		return repository;
	}

	/**
	 * CORS 설정 빈
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:8081")); // 프론트엔드 URL
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}
}