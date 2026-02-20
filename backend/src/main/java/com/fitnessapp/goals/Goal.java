package com.fitnessapp.goals;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Table("goals")
@Getter
@Setter
public class Goal {

    @Id
    private UUID id;

    private UUID userId;

    private GoalType goalType;

    private BigDecimal targetValue;

    private String unit;

    private LocalDate targetDate;

    private Instant createdAt;

    private Instant updatedAt;
}
