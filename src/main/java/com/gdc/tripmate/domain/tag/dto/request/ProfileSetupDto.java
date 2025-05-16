package com.gdc.tripmate.domain.tag.dto.request;

import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 회원가입 시 프로필 설정을 위한 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileSetupDto {

	private String nickname;
	private String bio;
	private String gender;
	private LocalDate birthDate;
	private List<String> tags;
}