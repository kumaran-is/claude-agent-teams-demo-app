package com.fitnessapp.security;

import io.netty.channel.ConnectTimeoutException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

/**
 * WebFilter that extracts a Firebase Bearer token from the Authorization header,
 * verifies it, and injects a FirebaseAuthentication into the reactive SecurityContext.
 *
 * Requests without a Bearer token are passed through — the SecurityConfig
 * determines which paths require authentication.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class FirebaseJwtAuthFilter implements WebFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final FirebaseTokenVerifier tokenVerifier;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            return chain.filter(exchange);
        }

        String idToken = authHeader.substring(BEARER_PREFIX.length());

        return tokenVerifier.verify(idToken)
                .flatMap(firebaseToken -> {
                    FirebaseAuthentication auth = new FirebaseAuthentication(firebaseToken);
                    log.debug("Authenticated Firebase user: uid={}", firebaseToken.getUid());
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                })
                .onErrorResume(InvalidFirebaseTokenException.class, ex -> {
                    log.warn("Rejected request — invalid Firebase token: path={}, reason={}",
                            exchange.getRequest().getPath(), ex.getMessage());
                    exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(ConnectTimeoutException.class, ex -> {
                    // Firebase JWKS endpoint unreachable — fail open with 503
                    log.error("Firebase JWKS endpoint unreachable: path={}", exchange.getRequest().getPath(), ex);
                    exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(Exception.class, ex -> {
                    // Unexpected error during token verification — fail closed with 500
                    log.error("Unexpected error during Firebase token verification: path={}",
                            exchange.getRequest().getPath(), ex);
                    exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
                    return exchange.getResponse().setComplete();
                });
    }
}
