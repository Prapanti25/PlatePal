package com.platepal.planner;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.platepal.profile.ProfileResponse;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@Component
public class MealPlanPromptBuilder {
    private final ObjectMapper objectMapper;

    public MealPlanPromptBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public String build(ProfileResponse profile) throws JacksonException {
        Map<String, Object> constraints = new LinkedHashMap<>();
        constraints.put("dailyCalorieTarget", profile.calorieTarget());
        constraints.put("householdSize", profile.householdSize());
        constraints.put("weeklyBudgetBDT", profile.weeklyBudgetBdt());
        constraints.put("dietaryRestrictions", profile.dietaryTags());

        return "Create a practical seven-day meal plan. Quantities and estimated costs must cover the whole household. "
                + "Return only a JSON object with a days array. Each of seven days must contain dayNumber 1 through 7, "
                + "exactly three meals with mealType BREAKFAST, LUNCH, DINNER, and one snack object. "
                + "Each recipe must contain mealType, name, prepMinutes, estimatedCostBdt, ingredients, and instructions. "
                + "Each ingredient must contain name, category, quantity, and unit. Use budget estimates in BDT. "
                + "Dietary and calorie values are planning constraints, not medical advice. Constraints JSON: "
                + objectMapper.writeValueAsString(constraints);
    }
}