package com.fitnessapp.notifications;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

@Table("notification_preferences")
@Getter
@Setter
public class NotificationPreference {

    @Id
    private UUID id;

    private UUID userId;

    private String fcmToken;

    private boolean remindersEnabled;

    private LocalTime reminderTime;

    private Instant updatedAt;
}
