package com.fitnessapp.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.credentials-path}")
    private String credentialsPath;

    @Value("${firebase.project-id}")
    private String projectId;

    @Bean
    public FirebaseApp firebaseApp() throws IOException {
        if (!FirebaseApp.getApps().isEmpty()) {
            log.info("Firebase app already initialised, reusing existing instance");
            return FirebaseApp.getInstance();
        }

        InputStream credentialsStream = loadCredentials();
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(credentialsStream))
                .setProjectId(projectId)
                .build();

        FirebaseApp app = FirebaseApp.initializeApp(options);
        log.info("Firebase app initialised for project: {}", projectId);
        return app;
    }

    private InputStream loadCredentials() throws IOException {
        // Try classpath first (useful for test/CI), then filesystem
        InputStream classpathStream = getClass().getClassLoader().getResourceAsStream(credentialsPath);
        if (classpathStream != null) {
            log.debug("Loading Firebase credentials from classpath: {}", credentialsPath);
            return classpathStream;
        }
        log.debug("Loading Firebase credentials from filesystem: {}", credentialsPath);
        return new FileInputStream(credentialsPath);
    }
}
