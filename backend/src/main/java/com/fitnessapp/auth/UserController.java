package com.fitnessapp.auth;

import com.fitnessapp.auth.dto.RegisterRequest;
import com.fitnessapp.auth.dto.UserResponse;
import com.fitnessapp.security.FirebaseAuthentication;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    /**
     * POST /api/v1/auth/register
     * Creates or syncs the Firebase user to the local DB.
     * Idempotent — safe to call on every app launch.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.OK)
    public Mono<UserResponse> register(
            @AuthenticationPrincipal FirebaseAuthentication auth,
            @Valid @RequestBody(required = false) RegisterRequest request) {
        return userService.registerOrSync(auth, request);
    }

    /**
     * GET /api/v1/auth/profile
     * Returns the authenticated user's profile from the DB.
     */
    @GetMapping("/profile")
    public Mono<UserResponse> getProfile(
            @AuthenticationPrincipal FirebaseAuthentication auth) {
        return userService.getProfile(auth);
    }
}
