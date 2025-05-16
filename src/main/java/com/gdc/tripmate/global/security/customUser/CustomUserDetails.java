package com.gdc.tripmate.global.security.customUser;

import java.util.Collection;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

/**
 * Spring Security의 User 클래스를 확장하여 사용자 ID 필드 추가
 */
public class CustomUserDetails extends User {

	private final Long id;

	public CustomUserDetails(String username, String password,
			Collection<? extends GrantedAuthority> authorities,
			Long id) {
		super(username, password, authorities);
		this.id = id;
	}

	/**
	 * 사용자 ID 반환
	 */
	public Long getId() {
		return id;
	}
}