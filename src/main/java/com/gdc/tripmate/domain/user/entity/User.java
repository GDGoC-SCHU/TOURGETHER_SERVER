package com.gdc.tripmate.domain.user.entity;

import com.gdc.tripmate.domain.tag.entity.UserProfile;
import com.gdc.tripmate.domain.user.status.AuthProvider;
import com.gdc.tripmate.domain.user.status.Status;
import com.gdc.tripmate.global.TimeStamp;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 사용자(User) 엔티티 - OAuth2 인증 정보 중심
 */
@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends TimeStamp {

	/**
	 * 컬럼 - 연관관계 컬럼을 제외한 컬럼을 정의합니다.
	 */
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// 이메일
	@Column(nullable = false, unique = true)
	private String email;

	// 휴대폰 번호
	@Column
	private String phoneNumber;

	// 사용자 실제 이름
	@Column(nullable = false)
	private String name;

	// 프로필 이미지 URL
	@Column
	private String picture;

	// 사용자 상태
	@Column
	@Enumerated(EnumType.STRING)
	private Status status = Status.PENDING;

	// 휴대폰 인증 여부
	@Column
	private boolean phoneVerified = false;

	// 인증 제공자
	@Enumerated(EnumType.STRING)
	private AuthProvider provider;

	// 제공자 ID
	private String providerId;

	// 사용자 역할
	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> roles = new ArrayList<>();

	/**
	 * 연관관계
	 */
	@OneToOne(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
	private UserProfile profile;

	/**
	 * 생성자
	 */
	@Builder
	public User(String email, String name, String picture, AuthProvider provider,
			String providerId) {
		this.email = email;
		this.name = name;
		this.picture = picture;
		this.provider = provider;
		this.providerId = providerId;
		this.roles = Collections.singletonList("ROLE_USER");
	}

	/**
	 * 서비스 메소드 - 외부에서 엔티티를 수정할 메소드를 정의합니다.
	 */
	public User update(String name, String picture) {
		this.name = name;
		this.picture = picture;
		return this;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setStatus(Status status) {
		this.status = status;
	}

	public void setPhoneVerified(boolean phoneVerified) {
		this.phoneVerified = phoneVerified;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	/**
	 * 연관관계 편의 메소드
	 */
	public void setProfile(UserProfile profile) {
		this.profile = profile;
		if (profile != null && profile.getUser() != this) {
			profile.setUser(this);
		}
	}
}