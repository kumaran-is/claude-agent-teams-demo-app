package com.fitnessapp.workouts.dto;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

public record MetricsResponse(
        long weeklySessions,
        BigDecimal weeklyVolumeKg,
        int currentStreak,
        Map<UUID, BigDecimal> personalRecords
) {}
