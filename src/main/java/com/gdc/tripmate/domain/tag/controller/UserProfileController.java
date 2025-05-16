package com.gdc.tripmate.domain.tag.controller;

import com.gdc.tripmate.domain.tag.dto.request.NicknameCheckDto;
import com.gdc.tripmate.domain.tag.dto.request.ProfileDto;
import com.gdc.tripmate.domain.tag.dto.request.ProfileSetupDto;
import com.gdc.tripmate.domain.tag.service.UserProfileService;
import com.gdc.tripmate.global.error.ErrorResponse;
import com.gdc.tripmate.global.util.CurrentUser;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 프로필 관련 API를 처리하는 컨트롤러
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Slf4j
public class UserProfileController {

	private final UserProfileService userProfileService;

	/**
	 * 닉네임 중복 확인 API - 회원가입 과정에서 호출
	 */
	@GetMapping("/user/nickname")
	public ResponseEntity<NicknameCheckDto> checkNickname(@RequestParam String nickname) {
		NicknameCheckDto result = userProfileService.checkNickname(nickname);
		return ResponseEntity.ok(result);
	}

	/**
	 * 사용자 프로필 설정 API (회원가입 완료) - JSON 요청 처리
	 * OAuth 인증 후 프로필 정보 설정 (이미지 없는 경우)
	 */
	@PostMapping(value = "/user/register", consumes = MediaType.APPLICATION_JSON_VALUE)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> setupProfileJson(
			@CurrentUser Long userId,
			@RequestBody ProfileSetupDto profileSetupDto) {
		
		try {
			// 서비스 호출하여 프로필 설정 (이미지 없음)
			ProfileDto completedProfile = userProfileService.setupProfile(userId, profileSetupDto, null);
			return ResponseEntity.ok(completedProfile);
		} catch (IllegalArgumentException e) {
			ErrorResponse errorResponse = ErrorResponse.builder()
					.status(HttpStatus.BAD_REQUEST.value())
					.message(e.getMessage())
					.build();
			return ResponseEntity.badRequest().body(errorResponse);
		} catch (Exception e) {
			ErrorResponse errorResponse = ErrorResponse.builder()
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.message("회원가입 처리 중 오류가 발생했습니다.")
					.detail(e.getMessage())
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * 사용자 프로필 설정 API (회원가입 완료) - 멀티파트 요청 처리
	 * OAuth 인증 후 프로필 정보 설정 (이미지 포함)
	 * 현재는 API 호환성을 위해 유지하지만 사용되지 않습니다.
	 */
	@PostMapping(value = "/user/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<?> setupProfileMultipart(
			@CurrentUser Long userId,
			@RequestPart(value = "profileData", required = false) ProfileSetupDto profileSetupDto,
			@RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
		
		try {
			// 프로필 DTO가 없는 경우 예외 처리
			if (profileSetupDto == null) {
				throw new IllegalArgumentException("프로필 데이터가 필요합니다.");
			}
			
			// 서비스 호출하여 프로필 설정
			ProfileDto completedProfile = userProfileService.setupProfile(userId, profileSetupDto, profileImage);
			
			return ResponseEntity.ok(completedProfile);
		} catch (IllegalArgumentException e) {
			ErrorResponse errorResponse = ErrorResponse.builder()
					.status(HttpStatus.BAD_REQUEST.value())
					.message(e.getMessage())
					.build();
			return ResponseEntity.badRequest().body(errorResponse);
		} catch (Exception e) {
			ErrorResponse errorResponse = ErrorResponse.builder()
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.message("회원가입 처리 중 오류가 발생했습니다.")
					.detail(e.getMessage())
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * 사용자 프로필 조회 API
	 */
	@GetMapping("/profile/{userId}")
	@PreAuthorize("@userProfileSecurity.isCurrentUserOrAdmin(#userId)")
	public ResponseEntity<ProfileDto> getProfile(@PathVariable Long userId) {
		ProfileDto profile = userProfileService.getProfile(userId);
		return ResponseEntity.ok(profile);
	}

	/**
	 * 현재 사용자 프로필 조회 API
	 */
	@GetMapping("/profile/me")
	@PreAuthorize("isAuthenticated()")
	public ResponseEntity<ProfileDto> getMyProfile(@CurrentUser Long userId) {
		ProfileDto profile = userProfileService.getProfile(userId);
		return ResponseEntity.ok(profile);
	}

	/**
	 * 사용자 프로필 업데이트 API (프로필 수정)
	 */
	@PutMapping(value = "/profile/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("@userProfileSecurity.isCurrentUserOrAdmin(#userId)")
	public ResponseEntity<?> updateProfile(
			@PathVariable Long userId,
			@RequestPart(value = "profileData") ProfileDto profileDto,
			@RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {

		try {
			ProfileDto updatedProfile = userProfileService.updateProfile(userId, profileDto,
					profileImage);
			return ResponseEntity.ok(updatedProfile);
		} catch (IllegalArgumentException e) {
			log.error("프로필 업데이트 중 입력값 오류: {}", e.getMessage(), e);
			ErrorResponse errorResponse = ErrorResponse.builder()
					.status(HttpStatus.BAD_REQUEST.value())
					.message(e.getMessage())
					.build();
			return ResponseEntity.badRequest().body(errorResponse);
		} catch (Exception e) {
			log.error("프로필 업데이트 중 오류 발생: {}", e.getMessage(), e);
			ErrorResponse errorResponse = ErrorResponse.builder()
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.message("프로필 업데이트 중 오류가 발생했습니다.")
					.detail(e.getMessage())
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}

	/**
	 * 프로필 이미지만 업데이트 API
	 */
	@PutMapping(value = "/profile/{userId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@PreAuthorize("@userProfileSecurity.isCurrentUserOrAdmin(#userId)")
	public ResponseEntity<?> updateProfileImage(
			@PathVariable Long userId,
			@RequestParam("image") MultipartFile image) {

		try {
			String imageUrl = userProfileService.updateProfileImage(userId, image);
			return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
		} catch (Exception e) {
			log.error("프로필 이미지 업데이트 중 오류 발생: {}", e.getMessage(), e);
			ErrorResponse errorResponse = ErrorResponse.builder()
					.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
					.message("프로필 이미지 업데이트 중 오류가 발생했습니다.")
					.detail(e.getMessage())
					.build();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
		}
	}
}