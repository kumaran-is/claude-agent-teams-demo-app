package com.fitnessapp.notifications.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

public record RegisterTokenRequest(
        @NotBlank @Size(max = 512) String fcmToken,
        @NotNull Boolean remindersEnabled,
        LocalTime reminderTime
) {}
