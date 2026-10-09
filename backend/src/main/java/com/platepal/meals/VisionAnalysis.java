package com.platepal.meals;

import java.math.BigDecimal;

public record VisionAnalysis(
        String dishName,
        Integer estimatedCalories,
        BigDecimal proteinGrams,
        BigDecimal carbsGrams,
        BigDecimal fatsGrams) {
}