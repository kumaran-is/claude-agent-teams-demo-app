package com.fitnessapp.nutrition;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Table("nutrition_logs")
@Getter
@Setter
public class NutritionLog {

    @Id
    private UUID id;

    private UUID userId;

    private LocalDate loggedAt;

    private String mealName;

    private BigDecimal calories;

    private BigDecimal proteinG;

    private BigDecimal carbsG;

    private BigDecimal fatG;

    private String notes;
}
