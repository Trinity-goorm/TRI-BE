package com.trinity.ctc.global.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.trinity.ctc.global.exception.CustomException;
import com.trinity.ctc.global.exception.error_code.FcmErrorCode;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class FirebaseInitializer {

    @Value("${firebase.credentials}")
    private String firebaseCredentials;

    @PostConstruct
    public void initialize() {
        try (InputStream serviceAccount = new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8))) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .setThreadManager(new CustomThreadManager())
                    .build();

            FirebaseApp.initializeApp(options);
        } catch (IOException e) {
            throw new CustomException(FcmErrorCode.FIREBASE_INITIALIZATION_FAILED);
        }
    }
}