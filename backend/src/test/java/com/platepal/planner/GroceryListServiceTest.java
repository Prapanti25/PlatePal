package com.platepal.planner;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.platepal.profile.AppUser;

class GroceryListServiceTest {
    @Test
    void aggregatesCompatibleMassAndVolumeUnits() {
        MealPlan plan = new MealPlan(new AppUser("firebase-user"), LocalDate.now(), LocalDate.now().plusDays(6), false);
        plan.addRecipe(new RecipeEntity((short) 1, new RecipeDraft("LUNCH", "Dal", 20, BigDecimal.TEN,
                List.of(
                        new Ingredient("Onion", "Produce", new BigDecimal("500"), "g"),
                        new Ingredient("Olive oil", "Pantry", new BigDecimal("2"), "tbsp")),
                List.of("Cook"))));
        plan.addRecipe(new RecipeEntity((short) 2, new RecipeDraft("DINNER", "Curry", 30, BigDecimal.TEN,
                List.of(
                        new Ingredient("onion", "produce", new BigDecimal("0.5"), "kg"),
                        new Ingredient("olive oil", "pantry", new BigDecimal("30"), "ml")),
                List.of("Cook"))));

        List<GroceryListService.AggregatedItem> items = GroceryListService.aggregate(plan);

        assertEquals(2, items.size());
        GroceryListService.AggregatedItem onion = items.stream().filter(item -> item.name().equals("Onion")).findFirst().orElseThrow();
        assertEquals(new BigDecimal("1"), onion.quantity());
        assertEquals("kg", onion.unit());
        GroceryListService.AggregatedItem oil = items.stream().filter(item -> item.name().equals("Olive oil")).findFirst().orElseThrow();
        assertEquals(0, oil.quantity().compareTo(new BigDecimal("60")));
        assertEquals("ml", oil.unit());
    }
}