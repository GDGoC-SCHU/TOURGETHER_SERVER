package com.gdc.tripmate.domain.user.service;

import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.repository.UserRepository;
import com.gdc.tripmate.global.security.oauth.OAuthAttributes;
import java.util.Collections;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

	private final UserRepository userRepository;

	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2UserService<OAuth2UserRequest, OAuth2User> delegate = new DefaultOAuth2UserService();
		OAuth2User oAuth2User = delegate.loadUser(userRequest);

		// 현재 로그인 진행 중인 서비스를 구분하는 코드
		String registrationId = userRequest.getClientRegistration().getRegistrationId();

		// OAuth2 로그인 진행 시 키가 되는 필드값 (PK)
		String userNameAttributeName = userRequest.getClientRegistration()
				.getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

		// OAuth2UserService를 통해 가져온 OAuth2User의 attribute를 담을 클래스
		OAuthAttributes attributes = OAuthAttributes.of(
				registrationId, userNameAttributeName, oAuth2User.getAttributes());

		User user = saveOrUpdate(attributes);

		return new DefaultOAuth2User(
				Collections.singleton(new SimpleGrantedAuthority(user.getRoles().get(0))),
				attributes.getAttributes(),
				attributes.getNameAttributeKey());
	}

	private User saveOrUpdate(OAuthAttributes attributes) {
		User user = userRepository.findByEmail(attributes.getEmail())
				.map(entity -> entity.update(attributes.getName(), attributes.getPicture()))
				.orElse(attributes.toEntity());

		// 랜덤 닉네임 생성 (이미 존재하는 닉네임이 없을 때만)
		if (user.getNickname() == null || user.getNickname().isEmpty()) {
			String randomNickname = generateUniqueNickname();
			user.setNickname(randomNickname);
		}

		return userRepository.save(user);
	}

	private String generateUniqueNickname() {
		String nickname;
		boolean isUnique = false;

		do {
			nickname = "User" + UUID.randomUUID().toString().substring(0, 8);
			isUnique = !userRepository.existsByNickname(nickname);
		} while (!isUnique);

		return nickname;
	}
}