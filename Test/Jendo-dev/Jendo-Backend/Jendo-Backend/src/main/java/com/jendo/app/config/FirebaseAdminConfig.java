package com.jendo.app.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class FirebaseAdminConfig {

    @PostConstruct
    public void init() throws IOException {
        if (FirebaseApp.getApps().isEmpty()) {
            String credsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");

            if (credsPath == null || credsPath.isEmpty()) {
                log.error("GOOGLE_APPLICATION_CREDENTIALS environment variable not set!");
                throw new IllegalStateException(
                        "GOOGLE_APPLICATION_CREDENTIALS environment variable not set. " +
                                "Please set it to your Firebase Admin SDK JSON file path."
                );
            }

            log.info("Initializing Firebase with credentials from: {}", credsPath);

            try (FileInputStream serviceAccount = new FileInputStream(credsPath)) {
                GoogleCredentials credentials = GoogleCredentials.fromStream(serviceAccount);

                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(credentials)
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("✅ Firebase Admin SDK initialized successfully");

            } catch (IOException e) {
                log.error("❌ Failed to initialize Firebase Admin SDK", e);
                throw new IllegalStateException("Failed to initialize Firebase", e);
            }
        } else {
            log.info("Firebase App already initialized");
        }
    }
}