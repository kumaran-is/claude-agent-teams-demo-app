package com.fitnessapp.security;

import com.google.firebase.auth.FirebaseToken;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

/**
 * Spring Security authentication token carrying Firebase token claims.
 * Stored in the reactive SecurityContext after successful JWT verification.
 */
public class FirebaseAuthentication extends AbstractAuthenticationToken {

    private final FirebaseToken firebaseToken;

    public FirebaseAuthentication(FirebaseToken firebaseToken) {
        super(List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.firebaseToken = firebaseToken;
        setAuthenticated(true);
    }

    /** Returns the Firebase UID — used as principal throughout the app. */
    @Override
    public String getPrincipal() {
        return firebaseToken.getUid();
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    public String getEmail() {
        return firebaseToken.getEmail();
    }

    public String getName() {
        return firebaseToken.getName();
    }

    public FirebaseToken getFirebaseToken() {
        return firebaseToken;
    }
}
