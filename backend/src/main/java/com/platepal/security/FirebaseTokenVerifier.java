package com.platepal.security;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

@Component
public class FirebaseTokenVerifier {
    private final FirebaseAuth firebaseAuth;

    public FirebaseTokenVerifier(
            @Value("${firebase.service-account-json:}") String serviceAccountJson,
            @Value("${firebase.project-id:}") String projectId) throws IOException {
        if (!StringUtils.hasText(serviceAccountJson)) {
            firebaseAuth = null;
            return;
        }

        FirebaseOptions.Builder options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(
                        serviceAccountJson.getBytes(StandardCharsets.UTF_8))));
        if (StringUtils.hasText(projectId)) {
            options.setProjectId(projectId);
        }

        Optional<FirebaseApp> existingApp = FirebaseApp.getApps().stream()
                .filter(app -> "platepal-auth".equals(app.getName()))
                .findFirst();
        FirebaseApp app = existingApp.orElseGet(() -> FirebaseApp.initializeApp(options.build(), "platepal-auth"));
        firebaseAuth = FirebaseAuth.getInstance(app);
    }

    public boolean isConfigured() {
        return firebaseAuth != null;
    }

    public FirebaseToken verifyIdToken(String token) throws FirebaseAuthException {
        if (firebaseAuth == null) {
            throw new IllegalStateException("Firebase Admin credentials are not configured");
        }
        return firebaseAuth.verifyIdToken(token);
    }
}