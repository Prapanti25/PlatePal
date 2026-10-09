package com.platepal.planner;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.List;
import java.util.Locale;
import java.util.stream.IntStream;

public record MealPlanView(
        Long id,
        LocalDate startsOn,
        LocalDate endsOn,
        boolean fallbackUsed,
        List<PlanDayView> days) {

    static MealPlanView from(MealPlan plan) {
        List<PlanDayView> days = IntStream.rangeClosed(1, 7)
                .mapToObj(dayNumber -> {
                    LocalDate date = plan.getStartsOn().plusDays(dayNumber - 1L);
                    List<RecipeView> meals = plan.getRecipes().stream()
                            .filter(recipe -> recipe.getDayOfWeek() == dayNumber)
                            .filter(recipe -> !"SNACK".equals(recipe.getMealType()))
                            .map(RecipeView::from)
                            .toList();
                    RecipeView snack = plan.getRecipes().stream()
                            .filter(recipe -> recipe.getDayOfWeek() == dayNumber)
                            .filter(recipe -> "SNACK".equals(recipe.getMealType()))
                            .findFirst()
                            .map(RecipeView::from)
                            .orElse(null);
                    return new PlanDayView(dayNumber, date, date.getDayOfWeek().getDisplayName(TextStyle.FULL, Locale.ENGLISH), meals, snack);
                })
                .toList();
        return new MealPlanView(plan.getId(), plan.getStartsOn(), plan.getEndsOn(), plan.isFallback(), days);
    }

    public record PlanDayView(int dayNumber, LocalDate date, String dayName, List<RecipeView> meals, RecipeView snack) {
    }

    public record RecipeView(
            Long id,
            String mealType,
            String name,
            Integer prepMinutes,
            BigDecimal estimatedCostBdt,
            List<Ingredient> ingredients,
            List<String> instructions) {
        static RecipeView from(RecipeEntity recipe) {
            return new RecipeView(recipe.getId(), recipe.getMealType(), recipe.getName(), recipe.getPrepMinutes(),
                    recipe.getEstimatedCostBdt(), recipe.getIngredients(), recipe.getInstructions());
        }
    }
}