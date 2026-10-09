package com.platepal.meals;

import java.math.BigDecimal;
import java.time.Instant;

public record LoggedMealResponse(
        Long id,
        String imageUrl,
        String dishName,
        Integer estimatedCalories,
        BigDecimal proteinGrams,
        BigDecimal carbsGrams,
        BigDecimal fatsGrams,
        Instant loggedAt) {
    static LoggedMealResponse from(LoggedMeal meal) {
        return new LoggedMealResponse(meal.getId(), meal.getImageUrl(), meal.getDishName(), meal.getEstimatedCalories(),
                meal.getProteinGrams(), meal.getCarbsGrams(), meal.getFatsGrams(), meal.getLoggedAt());
    }
}