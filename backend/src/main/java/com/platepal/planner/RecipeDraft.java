package com.platepal.planner;

import java.math.BigDecimal;
import java.util.List;

public record RecipeDraft(
        String mealType,
        String name,
        Integer prepMinutes,
        BigDecimal estimatedCostBdt,
        List<Ingredient> ingredients,
        List<String> instructions) {
}