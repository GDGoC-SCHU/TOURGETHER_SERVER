package com.gdc.tripmate.global.security.oauth;

import com.gdc.tripmate.domain.user.entity.User;
import com.gdc.tripmate.domain.user.status.AuthProvider;
import java.util.Map;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OAuthAttributes {

	private Map<String, Object> attributes;
	private String nameAttributeKey;
	private String name;
	private String email;
	private String picture;
	private AuthProvider provider;

	@Builder
	public OAuthAttributes(Map<String, Object> attributes, String nameAttributeKey, String name,
			String email, String picture, AuthProvider provider) {
		this.attributes = attributes;
		this.nameAttributeKey = nameAttributeKey;
		this.name = name;
		this.email = email;
		this.picture = picture;
		this.provider = provider;
	}

	public static OAuthAttributes of(String registrationId, String userNameAttributeName,
			Map<String, Object> attributes) {
		if ("kakao".equals(registrationId.toLowerCase())) {
			return ofKakao(userNameAttributeName, attributes);
		}

		return ofGoogle(userNameAttributeName, attributes);
	}

	private static OAuthAttributes ofGoogle(String userNameAttributeName,
			Map<String, Object> attributes) {
		return OAuthAttributes.builder()
				.name((String) attributes.get("name"))
				.email((String) attributes.get("email"))
				.picture((String) attributes.get("picture"))
				.attributes(attributes)
				.nameAttributeKey(userNameAttributeName)
				.provider(AuthProvider.GOOGLE)
				.build();
	}

	private static OAuthAttributes ofKakao(String userNameAttributeName,
			Map<String, Object> attributes) {
		Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
		Map<String, Object> kakaoProfile = (Map<String, Object>) kakaoAccount.get("profile");

		return OAuthAttributes.builder()
				.name((String) kakaoProfile.get("nickname"))
				.email((String) kakaoAccount.get("email"))
				.picture((String) kakaoProfile.get("profile_image_url"))
				.attributes(attributes)
				.nameAttributeKey(userNameAttributeName)
				.provider(AuthProvider.KAKAO)
				.build();
	}

	public User toEntity() {
		return User.builder()
				.name(name)
				.email(email)
				.picture(picture)
				.provider(provider)
				.providerId(String.valueOf(attributes.get(nameAttributeKey)))
				.build();
	}
}