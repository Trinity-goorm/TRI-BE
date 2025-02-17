package com.trinity.ctc.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.trinity.ctc.util.exception.CustomException;
import com.trinity.ctc.util.exception.error_code.FirebaseErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;

@Slf4j
@Component
public class FirebaseInitializer {

    @Value("${firebase.key-path}")
    private String fcmKeyPath;

    @PostConstruct
    public void initialize() {
        try (InputStream serviceAccount = new ClassPathResource(fcmKeyPath).getInputStream()) {

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseOptions options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                        .build();

                FirebaseApp.initializeApp(options);
                log.info("Firebase initialized successfully.");
            } else {
                log.info("Firebase already initialized.");
            }

        } catch (IOException e) {
            throw new CustomException(FirebaseErrorCode.FIREBASE_INITIALIZATION_FAILED);
        }
    }
}
