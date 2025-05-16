package com.gdc.tripmate.domain.tag.dto.request;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 사용자 프로필 정보를 담는 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileDto {

	private Long userId;
	private String email;
	private String nickname;
	private String bio;
	private String gender;
	private LocalDate birthDate;
	private String profileImageUrl;
	private String phoneNumber;
	private boolean phoneVerified;
	private List<String> tags;
	private boolean profileCompleted;
}