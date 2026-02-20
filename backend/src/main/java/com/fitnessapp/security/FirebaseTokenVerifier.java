package com.fitnessapp.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@Slf4j
public class FirebaseTokenVerifier {

    /**
     * Verifies a Firebase ID token and returns the decoded token claims.
     * The Firebase SDK verification call is blocking, so we offload it to a
     * bounded elastic scheduler to avoid blocking the Netty event loop.
     */
    public Mono<FirebaseToken> verify(String idToken) {
        return Mono.fromCallable(() -> {
                    try {
                        return FirebaseAuth.getInstance().verifyIdToken(idToken);
                    } catch (FirebaseAuthException e) {
                        log.warn("Firebase token verification failed: code={}, message={}", e.getAuthErrorCode(), e.getMessage());
                        throw new InvalidFirebaseTokenException("Firebase token invalid or expired: " + e.getMessage());
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }
}
