package com.fitnessapp.auth;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<User, UUID> {

    Mono<User> findByFirebaseUid(String firebaseUid);

    Mono<User> findByEmail(String email);
}
