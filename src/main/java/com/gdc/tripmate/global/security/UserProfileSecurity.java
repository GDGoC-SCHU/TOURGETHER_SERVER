package com.gdc.tripmate.global.security;

import com.gdc.tripmate.global.security.customUser.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * 사용자 프로필 관련 보안 검사를 처리하는 컴포넌트
 */
@Component("userProfileSecurity")
@RequiredArgsConstructor
public class UserProfileSecurity {

	/**
	 * 현재 인증된 사용자가 요청한 사용자 ID의 주인이거나 관리자인지 확인
	 *
	 * @param userId 요청한 사용자 ID
	 * @return 권한 있음 여부
	 */
	public boolean isCurrentUserOrAdmin(Long userId) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || !authentication.isAuthenticated()) {
			return false;
		}

		// Principal이 CustomUserDetails 타입인 경우
		Object principal = authentication.getPrincipal();
		if (principal instanceof CustomUserDetails) {
			Long currentUserId = ((CustomUserDetails) principal).getId();

			// 요청한 사용자 ID와 현재 인증된 사용자 ID가 일치하는지 확인
			if (userId.equals(currentUserId)) {
				return true;
			}
		}

		// 관리자 권한이 있는지 확인
		return authentication.getAuthorities().stream()
				.map(GrantedAuthority::getAuthority)
				.anyMatch(authority -> authority.equals("ROLE_ADMIN"));
	}
}