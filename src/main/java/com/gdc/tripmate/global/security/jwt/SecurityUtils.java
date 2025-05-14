package com.gdc.tripmate.global.security.jwt;

import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

	private final UserRepository userRepository;

	/**
	 * 현재 인증된 사용자의 ID를 반환
	 */
	public Long getCurrentUserId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated() ||
				"anonymousUser".equals(authentication.getPrincipal())) {
			return null;
		}

		// UserDetails로 변환 가능한 경우
		if (authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String username = userDetails.getUsername();

			// 이메일로 사용자 조회
			return userRepository.findByEmail(username)
					.map(User::getId)
					.orElse(null);
		}

		// 기타 경우 (예: OAuth2 인증)
		return null;
	}

	/**
	 * 현재 인증된 사용자의 정보를 반환
	 */
	public User getCurrentUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated() ||
				"anonymousUser".equals(authentication.getPrincipal())) {
			return null;
		}

		// UserDetails로 변환 가능한 경우
		if (authentication.getPrincipal() instanceof UserDetails) {
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String username = userDetails.getUsername();

			// 이메일로 사용자 조회
			return userRepository.findByEmail(username).orElse(null);
		}

		return null;
	}
}