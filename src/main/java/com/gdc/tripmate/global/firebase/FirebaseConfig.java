package com.gdc.tripmate.global.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
@Slf4j
public class FirebaseConfig {

	// 전화번호 인증 세션 캐시 (실제 프로덕션에서는 Redis 등으로 대체)
	private final Map<String, String> verificationSessions = new ConcurrentHashMap<>();

	@Bean
	public FirebaseApp firebaseApp() throws IOException {
		if (FirebaseApp.getApps().isEmpty()) {
			GoogleCredentials credentials = GoogleCredentials
					.fromStream(new ClassPathResource(
							"tripmate-test-18915.json").getInputStream());

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(credentials)
					.build();

			return FirebaseApp.initializeApp(options);
		}
		return FirebaseApp.getInstance();
	}

	/**
	 * Firebase Auth 인스턴스 반환
	 */
	@Bean
	public FirebaseAuth firebaseAuth() throws IOException {
		return FirebaseAuth.getInstance(firebaseApp());
	}

	/**
	 * 전화번호 인증 코드 세션 생성 및 저장
	 *
	 * @param phoneNumber 전화번호
	 * @return 세션 ID
	 */
	public String createPhoneAuthSession(String phoneNumber) {
		// 실제 Firebase의 PhoneAuthProvider를 사용할 수 없으므로
		// 내부적으로 세션 ID 생성하여 관리
		String sessionId = generateSessionId();
		verificationSessions.put(sessionId, phoneNumber);
		return sessionId;
	}

	/**
	 * 세션 ID 생성
	 *
	 * @return 고유 세션 ID
	 */
	private String generateSessionId() {
		return java.util.UUID.randomUUID().toString();
	}

	/**
	 * 인증 세션 정리
	 *
	 * @param sessionId 세션 ID
	 */
	public void cleanupSession(String sessionId) {
		verificationSessions.remove(sessionId);
	}
}