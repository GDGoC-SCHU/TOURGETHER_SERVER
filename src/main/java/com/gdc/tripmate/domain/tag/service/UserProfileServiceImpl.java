package com.gdc.tripmate.domain.tag.service;

import com.gdc.tripmate.domain.tag.dto.request.NicknameCheckDto;
import com.gdc.tripmate.domain.tag.dto.request.ProfileDto;
import com.gdc.tripmate.domain.tag.dto.request.ProfileSetupDto;
import com.gdc.tripmate.domain.tag.entity.Tag;
import com.gdc.tripmate.domain.tag.entity.UserProfile;
import com.gdc.tripmate.domain.tag.repository.TagRepository;
import com.gdc.tripmate.domain.tag.repository.UserProfileRepository;
import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import com.gdc.tripmate.domain.user.status.Status;
import com.gdc.tripmate.global.error.ResourceNotFoundException;
import com.gdc.tripmate.global.util.FileUploadUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * 사용자 프로필 관련 비즈니스 로직을 처리하는 서비스 구현체
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileServiceImpl implements UserProfileService {

	private final UserRepository userRepository;
	private final UserProfileRepository userProfileRepository;
	private final TagRepository tagRepository;
	private final FileUploadUtil fileUploadUtil;

	@Value("${app.tag.categories:MBTI,HOBBY,INTEREST}")
	private String[] tagCategories;

	/**
	 * 닉네임 중복 확인
	 */
	@Override
	@Transactional(readOnly = true)
	public NicknameCheckDto checkNickname(String nickname) {
		if (nickname == null || nickname.trim().isEmpty()) {
			NicknameCheckDto result = new NicknameCheckDto();
			result.setAvailable(false);
			result.setMessage("닉네임을 입력해주세요.");
			return result;
		}

		boolean exists = userProfileRepository.existsByNickname(nickname.trim());
		
		NicknameCheckDto result = new NicknameCheckDto();
		result.setAvailable(!exists);
		result.setMessage(exists ? "이미 사용 중인 닉네임입니다." : "사용 가능한 닉네임입니다.");
		return result;
	}

	/**
	 * 사용자 프로필 조회
	 */
	@Override
	@Transactional(readOnly = true)
	public ProfileDto getProfile(Long userId) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

		UserProfile profile = userProfileRepository.findByUserId(userId)
				.orElse(null);

		// 프로필이 아직 없는 경우
		if (profile == null) {
			ProfileDto dto = new ProfileDto();
			dto.setUserId(user.getId());
			dto.setEmail(user.getEmail());
			dto.setProfileImageUrl(user.getPicture());
			dto.setPhoneNumber(user.getPhoneNumber());
			dto.setPhoneVerified(user.isPhoneVerified());
			dto.setProfileCompleted(false);
			return dto;
		}

		List<String> tagNames = profile.getProfileTags().stream()
				.map(profileTag -> profileTag.getTag().getName())
				.collect(Collectors.toList());

		ProfileDto dto = new ProfileDto();
		dto.setUserId(user.getId());
		dto.setEmail(user.getEmail());
		dto.setNickname(profile.getNickname());
		dto.setBio(profile.getBio());
		dto.setGender(profile.getGender());
		dto.setBirthDate(profile.getBirthDate());
		dto.setProfileImageUrl(user.getPicture());
		dto.setPhoneNumber(user.getPhoneNumber());
		dto.setPhoneVerified(user.isPhoneVerified());
		dto.setTags(tagNames);
		dto.setProfileCompleted(profile.isProfileCompleted());
		return dto;
	}

	/**
	 * 사용자 프로필 업데이트
	 */
	@Override
	@Transactional
	public ProfileDto updateProfile(Long userId, ProfileDto profileDto,
			MultipartFile profileImage) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

		UserProfile profile = userProfileRepository.findByUserId(userId)
				.orElseThrow(
						() -> new ResourceNotFoundException("프로필이 설정되지 않았습니다. 먼저 프로필을 설정해주세요."));

		// 닉네임 중복 확인 (변경된 경우에만)
		if (profileDto.getNickname() != null && !profileDto.getNickname()
				.equals(profile.getNickname())) {
			if (userProfileRepository.existsByNickname(profileDto.getNickname())) {
				throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
			}
			profile.setNickname(profileDto.getNickname());
		}

		// 기본 정보 업데이트
		if (profileDto.getBio() != null) {
			profile.setBio(profileDto.getBio());
		}

		if (profileDto.getGender() != null) {
			profile.setGender(profileDto.getGender());
		}

		if (profileDto.getBirthDate() != null) {
			profile.setBirthDate(profileDto.getBirthDate());
		}

		// 프로필 이미지 업로드 처리
		if (profileImage != null && !profileImage.isEmpty()) {
			try {
				String imageUrl = fileUploadUtil.uploadFile(profileImage, "profile");
				user.setPicture(imageUrl);
				userRepository.save(user);
			} catch (Exception e) {
				log.error("프로필 이미지 업로드 중 오류 발생: {}", e.getMessage(), e);
				// 이미지 업로드 실패해도 계속 진행
			}
		}

		// 태그 처리 - 전부 삭제 후 새로 등록
		if (profileDto.getTags() != null) {
			profile.removeTags(); // 기존 태그 제거

			for (String tagName : profileDto.getTags()) {
				try {
					Tag tag = tagRepository.findByName(tagName)
							.orElseGet(() -> tagRepository.save(new Tag(tagName)));
					profile.addTag(tag);
				} catch (Exception e) {
					log.error("태그 처리 중 오류 발생 (태그: {}): {}", tagName, e.getMessage(), e);
				}
			}
		}

		// 프로필 완성 여부 체크
		checkProfileCompleteness(user, profile);

		// 프로필 업데이트 후 저장
		UserProfile updatedProfile = userProfileRepository.save(profile);

		// 업데이트된 프로필 정보 반환
		List<String> tagNames = updatedProfile.getProfileTags().stream()
				.map(profileTag -> profileTag.getTag().getName())
				.collect(Collectors.toList());

		ProfileDto dto = new ProfileDto();
		dto.setUserId(user.getId());
		dto.setEmail(user.getEmail());
		dto.setNickname(updatedProfile.getNickname());
		dto.setBio(updatedProfile.getBio());
		dto.setGender(updatedProfile.getGender());
		dto.setBirthDate(updatedProfile.getBirthDate());
		dto.setProfileImageUrl(user.getPicture());
		dto.setPhoneNumber(user.getPhoneNumber());
		dto.setPhoneVerified(user.isPhoneVerified());
		dto.setTags(tagNames);
		dto.setProfileCompleted(updatedProfile.isProfileCompleted());
		return dto;
	}

	/**
	 * 프로필 설정 (회원가입 완료)
	 */
	@Override
	@Transactional
	public ProfileDto setupProfile(Long userId, ProfileSetupDto profileSetupDto,
			MultipartFile profileImage) {
		log.info("프로필 설정 시작 - userId: {}, nickname: {}", userId, profileSetupDto.getNickname());
		
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

		// 이미 프로필이 있는지 확인
		UserProfile profile = userProfileRepository.findByUserId(userId)
				.orElse(null);

		if (profile == null) {
			// 닉네임 중복 확인
			if (profileSetupDto.getNickname() != null) {
				if (userProfileRepository.existsByNickname(profileSetupDto.getNickname())) {
					throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
				}
			}

			// 새 프로필 생성
			profile = new UserProfile();
			profile.setNickname(profileSetupDto.getNickname());
			profile.setBio(profileSetupDto.getBio());
			profile.setGender(profileSetupDto.getGender());
			profile.setBirthDate(profileSetupDto.getBirthDate());
			profile.setUser(user);
		} else {
			// 기존 프로필 업데이트
			if (profileSetupDto.getNickname() != null && !profileSetupDto.getNickname()
					.equals(profile.getNickname())) {
				if (userProfileRepository.existsByNickname(profileSetupDto.getNickname())) {
					throw new IllegalArgumentException("이미 사용 중인 닉네임입니다.");
				}
				profile.setNickname(profileSetupDto.getNickname());
			}

			if (profileSetupDto.getBio() != null) {
				profile.setBio(profileSetupDto.getBio());
			}

			if (profileSetupDto.getGender() != null) {
				profile.setGender(profileSetupDto.getGender());
			}

			if (profileSetupDto.getBirthDate() != null) {
				profile.setBirthDate(profileSetupDto.getBirthDate());
			}
		}

		// 프로필 이미지 업로드 처리
		String imageUrl = null;
		if (profileImage != null && !profileImage.isEmpty()) {
			try {
				imageUrl = fileUploadUtil.uploadFile(profileImage, "profile");
				user.setPicture(imageUrl);
				userRepository.save(user);
				log.info("프로필 이미지 업로드 성공: {}", imageUrl);
			} catch (Exception e) {
				log.error("프로필 이미지 업로드 중 오류 발생: {}", e.getMessage(), e);
				// 이미지 업로드 실패해도 계속 진행
			}
		}

		// 태그 설정
		List<String> addedTags = new ArrayList<>();
		if (profileSetupDto.getTags() != null && !profileSetupDto.getTags().isEmpty()) {
			for (String tagName : profileSetupDto.getTags()) {
				try {
					Tag tag = tagRepository.findByName(tagName)
							.orElseGet(() -> {
								log.info("새 태그 생성: {}", tagName);
								return tagRepository.save(new Tag(tagName));
							});
					profile.addTag(tag);
					addedTags.add(tagName);
				} catch (Exception e) {
					log.error("태그 처리 중 오류 발생 (태그: {}): {}", tagName, e.getMessage(), e);
				}
			}
		}
		log.info("추가된 태그: {}", addedTags);

		// 프로필 완성 여부 체크
		checkProfileCompleteness(user, profile);

		// 프로필 저장
		UserProfile savedProfile = userProfileRepository.save(profile);
		log.info("프로필 저장 완료 - 프로필ID: {}, 완료 여부: {}", savedProfile.getId(), savedProfile.isProfileCompleted());

		// 설정된 프로필 정보 반환
		List<String> tagNames = savedProfile.getProfileTags().stream()
				.map(profileTag -> profileTag.getTag().getName())
				.collect(Collectors.toList());

		ProfileDto dto = new ProfileDto();
		dto.setUserId(user.getId());
		dto.setEmail(user.getEmail());
		dto.setNickname(savedProfile.getNickname());
		dto.setBio(savedProfile.getBio());
		dto.setGender(savedProfile.getGender());
		dto.setBirthDate(savedProfile.getBirthDate());
		dto.setProfileImageUrl(user.getPicture());
		dto.setPhoneNumber(user.getPhoneNumber());
		dto.setPhoneVerified(user.isPhoneVerified());
		dto.setTags(tagNames);
		dto.setProfileCompleted(savedProfile.isProfileCompleted());
		return dto;
	}

	/**
	 * 프로필 이미지만 업데이트
	 */
	@Override
	@Transactional
	public String updateProfileImage(Long userId, MultipartFile image) {
		User user = userRepository.findById(userId)
				.orElseThrow(() -> new ResourceNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

		if (image == null || image.isEmpty()) {
			throw new IllegalArgumentException("이미지 파일이 없습니다.");
		}

		String imageUrl = fileUploadUtil.uploadFile(image, "profile");
		user.setPicture(imageUrl);
		userRepository.save(user);

		return imageUrl;
	}


	/**
	 * 프로필 완성 여부 체크 프로필이 완성된 경우 사용자 상태도 업데이트
	 */
	private void checkProfileCompleteness(User user, UserProfile profile) {
		boolean isProfileComplete = profile.getNickname() != null &&
				profile.getBio() != null &&
				profile.getGender() != null &&
				profile.getBirthDate() != null &&
				user.isPhoneVerified() &&
				!profile.getProfileTags().isEmpty();

		profile.setProfileCompleted(isProfileComplete);

		// 프로필이 완성되면 사용자 상태를 ACTIVE로 변경
		if (isProfileComplete && user.getStatus() == Status.PENDING) {
			user.setStatus(Status.ACTIVE);
			userRepository.save(user);
		}
	}
}