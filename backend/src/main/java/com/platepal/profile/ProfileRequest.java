package com.platepal.profile;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ProfileRequest(
        @NotNull @Positive Integer calorieTarget,
        @NotNull @Positive Integer householdSize,
        @NotNull @DecimalMin("0.00") BigDecimal weeklyBudgetBdt,
        @NotNull List<String> dietaryTags) {
}