package com.gdc.tripmate.domain.tag.controller;

import com.gdc.tripmate.domain.tag.dto.request.NicknameCheckDto;
import com.gdc.tripmate.domain.tag.dto.request.ProfileDto;
import com.gdc.tripmate.domain.tag.dto.request.ProfileSetupDto;
import com.gdc.tripmate.domain.tag.service.UserProfileService;
import com.gdc.tripmate.global.util.CurrentUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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
     * 사용자 프로필 설정 API (회원가입 완료)
     * OAuth 인증 후 프로필 정보 한 번에 설정
     */
    @PostMapping(value = "/user/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ProfileDto> setupProfile(
            @CurrentUser Long userId,
            @RequestPart(value = "profileData") ProfileSetupDto profileSetupDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        
        ProfileDto completedProfile = userProfileService.setupProfile(userId, profileSetupDto, profileImage);
        return ResponseEntity.ok(completedProfile);
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
    public ResponseEntity<ProfileDto> updateProfile(
            @PathVariable Long userId,
            @RequestPart(value = "profileData") ProfileDto profileDto,
            @RequestPart(value = "profileImage", required = false) MultipartFile profileImage) {
        
        ProfileDto updatedProfile = userProfileService.updateProfile(userId, profileDto, profileImage);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * 프로필 이미지만 업데이트 API
     */
    @PutMapping(value = "/profile/{userId}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("@userProfileSecurity.isCurrentUserOrAdmin(#userId)")
    public ResponseEntity<Map<String, String>> updateProfileImage(
            @PathVariable Long userId,
            @RequestParam("image") MultipartFile image) {
        
        String imageUrl = userProfileService.updateProfileImage(userId, image);
        return ResponseEntity.ok(Map.of("imageUrl", imageUrl));
    }
}