package com.gdc.tripmate.domain.tag.service;

import com.gdc.tripmate.domain.tag.dto.request.NicknameCheckDto;
import com.gdc.tripmate.domain.tag.dto.request.ProfileDto;
import com.gdc.tripmate.domain.tag.dto.request.ProfileSetupDto;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 사용자 프로필 관련 비즈니스 로직을 정의하는 인터페이스
 */
public interface UserProfileService {

    /**
     * 닉네임 중복 확인
     * 
     * @param nickname 확인할 닉네임
     * @return 중복 확인 결과 DTO
     */
    NicknameCheckDto checkNickname(String nickname);
    
    /**
     * 사용자 프로필 조회
     * 
     * @param userId 사용자 ID
     * @return 프로필 정보 DTO
     */
    ProfileDto getProfile(Long userId);
    
    /**
     * 사용자 프로필 업데이트
     * 
     * @param userId 사용자 ID
     * @param profileDto 업데이트할 프로필 정보
     * @param profileImage 업로드된 프로필 이미지 (있는 경우)
     * @return 업데이트된 프로필 정보 DTO
     */
    ProfileDto updateProfile(Long userId, ProfileDto profileDto, MultipartFile profileImage);
    
    /**
     * 사용자 프로필 설정 (회원가입 완료)
     * 
     * @param userId 사용자 ID
     * @param profileSetupDto 설정할 프로필 정보
     * @param profileImage 업로드된 프로필 이미지 (있는 경우)
     * @return 설정된 프로필 정보 DTO
     */
    ProfileDto setupProfile(Long userId, ProfileSetupDto profileSetupDto, MultipartFile profileImage);
    
    /**
     * 프로필 이미지만 업데이트
     * 
     * @param userId 사용자 ID
     * @param image 업로드된 프로필 이미지
     * @return 이미지 URL
     */
    String updateProfileImage(Long userId, MultipartFile image);
}