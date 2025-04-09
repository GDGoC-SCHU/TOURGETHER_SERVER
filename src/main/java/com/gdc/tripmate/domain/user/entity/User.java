package com.gdc.tripmate.domain.user.entity;

import com.gdc.tripmate.domain.user.status.AuthProvider;
import com.gdc.tripmate.domain.user.status.Status;
import com.gdc.tripmate.global.TimeStamp;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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
	long id;

	// 이메일
	@Column(nullable = false, unique = true)
	private String email;

	// 휴대폰 번호
	@Column
	private String phoneNumber;

	@Column(nullable = false)
	private String name;

	// 설정 닉네임(이름)
	@Column(nullable = false, unique = true)
	private String nickname;

	private String picture;


	@Column
	private Status status = Status.PENDING;

	/**
	 * 생성자 - 약속된 형태로만 생성가능하도록 합니다.
	 */

	@Enumerated(EnumType.STRING)
	private AuthProvider provider;

	private String providerId;

	@ElementCollection(fetch = FetchType.EAGER)
	private List<String> roles = new ArrayList<>();

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
	 * 연관관계 - Foreign Key 값을 따로 컬럼으로 정의하지 않고 연관 관계로 정의합니다.
	 */

	/**
	 * 연관관계 편의 메소드 - 반대쪽에는 연관관계 편의 메소드가 없도록 주의합니다.
	 */

	/**
	 * 서비스 메소드 - 외부에서 엔티티를 수정할 메소드를 정의합니다. (단일 책임을 가지도록 주의합니다.)
	 */
	public User update(String name, String picture) {
		this.name = name;
		this.picture = picture;
		return this;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public void setStatus(Status status) {
		this.status = status;
	}
}
