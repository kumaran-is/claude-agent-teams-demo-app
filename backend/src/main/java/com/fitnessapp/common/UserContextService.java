package com.fitnessapp.common;

import com.fitnessapp.auth.UserRepository;
import com.fitnessapp.security.FirebaseAuthentication;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

/**
 * Resolves the internal DB user ID from a Firebase authentication token.
 * Centralises the repeated pattern across all domain services.
 */
@Service
@RequiredArgsConstructor
public class UserContextService {

    private final UserRepository userRepository;

    public Mono<UUID> resolveUserId(FirebaseAuthentication auth) {
        return userRepository.findByFirebaseUid(auth.getPrincipal())
                .map(u -> u.getId())
                .switchIfEmpty(Mono.error(new ResourceNotFoundException("User not found")));
    }
}
