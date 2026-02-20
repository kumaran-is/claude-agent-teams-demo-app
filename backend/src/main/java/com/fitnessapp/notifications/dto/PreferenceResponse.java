package com.fitnessapp.notifications.dto;

import java.time.Instant;
import java.time.LocalTime;
import java.util.UUID;

public record PreferenceResponse(
        UUID id,
        boolean remindersEnabled,
        LocalTime reminderTime,
        Instant updatedAt
) {}
