package com.fitnessapp.auth;

import com.fitnessapp.auth.dto.RegisterRequest;
import com.fitnessapp.auth.dto.UserResponse;
import com.fitnessapp.common.ResourceNotFoundException;
import com.fitnessapp.security.FirebaseAuthentication;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Register (or sync) the authenticated Firebase user to the local users table.
     * Performs an upsert: creates on first login, updates display_name on subsequent calls.
     */
    public Mono<UserResponse> registerOrSync(FirebaseAuthentication auth, RegisterRequest request) {
        String uid = auth.getPrincipal();
        String email = auth.getEmail();
        String displayName = resolveDisplayName(request, auth.getName());

        return userRepository.findByFirebaseUid(uid)
                .flatMap(existing -> {
                    // Update display name if provided
                    if (displayName != null && !displayName.isBlank()) {
                        existing.setDisplayName(displayName);
                        existing.setUpdatedAt(Instant.now());
                        return userRepository.save(existing);
                    }
                    return Mono.just(existing);
                })
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = new User();
                    newUser.setFirebaseUid(uid);
                    newUser.setEmail(email);
                    newUser.setDisplayName(displayName);
                    newUser.setCreatedAt(Instant.now());
                    newUser.setUpdatedAt(Instant.now());
                    log.info("Registering new user: firebaseUid={}", uid);
                    return userRepository.save(newUser);
                }))
                .map(this::toResponse);
    }

    /**
     * Returns the profile of the currently authenticated user.
     */
    public Mono<UserResponse> getProfile(FirebaseAuthentication auth) {
        return userRepository.findByFirebaseUid(auth.getPrincipal())
                .map(this::toResponse)
                .switchIfEmpty(Mono.error(new ResourceNotFoundException(
                        "User not found for firebaseUid=" + auth.getPrincipal()
                )));
    }

    private String resolveDisplayName(RegisterRequest request, String firebaseName) {
        if (request != null && request.displayName() != null && !request.displayName().isBlank()) {
            return request.displayName();
        }
        return firebaseName;
    }

    private UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getEmail(), user.getDisplayName(), user.getCreatedAt());
    }
}
