package com.fitnessapp.auth;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("users")
@Getter
@Setter
public class User {

    @Id
    private UUID id;

    private String firebaseUid;

    private String email;

    private String displayName;

    private Instant createdAt;

    private Instant updatedAt;
}
