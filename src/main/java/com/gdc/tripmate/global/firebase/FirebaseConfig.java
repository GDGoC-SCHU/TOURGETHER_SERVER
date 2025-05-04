package com.gdc.tripmate.global.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.IOException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
public class FirebaseConfig {

	@Bean
	public FirebaseApp firebaseApp() throws IOException {
		if (FirebaseApp.getApps().isEmpty()) {
			GoogleCredentials credentials = GoogleCredentials
					.fromStream(new ClassPathResource(
							"tripmate-test-18915-firebase-adminsdk-fbsvc-4c838f02ff.json").getInputStream());

			FirebaseOptions options = FirebaseOptions.builder()
					.setCredentials(credentials)
					.build();

			return FirebaseApp.initializeApp(options);
		}
		return FirebaseApp.getInstance();
	}
}