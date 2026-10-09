package com.platepal.planner;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class GroceryListService {
    private final MealPlanRepository mealPlanRepository;
    private final GroceryItemRepository groceryItemRepository;

    public GroceryListService(MealPlanRepository mealPlanRepository, GroceryItemRepository groceryItemRepository) {
        this.mealPlanRepository = mealPlanRepository;
        this.groceryItemRepository = groceryItemRepository;
    }

    @Transactional
    public void refreshForPlan(MealPlan plan) {
        groceryItemRepository.deleteAllByMealPlan_Id(plan.getId());
        List<GroceryItem> items = aggregate(plan).stream()
                .map(item -> new GroceryItem(plan, item.name(), item.category(), item.quantity(), item.unit()))
                .toList();
        groceryItemRepository.saveAll(items);
    }

    @Transactional(readOnly = true)
    public List<GroceryItemView> list(String firebaseUid) {
        return mealPlanRepository.findFirstByUser_FirebaseUidAndStatusOrderByStartsOnDesc(firebaseUid, "ACTIVE")
                .map(plan -> groceryItemRepository.findAllByMealPlan_IdOrderByCategoryAscNameAsc(plan.getId()).stream()
                        .map(GroceryItemView::from)
                        .toList())
                .orElseGet(List::of);
    }

    @Transactional
    public GroceryItemView updatePantry(String firebaseUid, Long itemId, boolean inPantry) {
        GroceryItem item = groceryItemRepository.findByIdAndMealPlan_User_FirebaseUid(itemId, firebaseUid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grocery item not found"));
        item.setInPantry(inPantry);
        return GroceryItemView.from(groceryItemRepository.save(item));
    }

    static List<AggregatedItem> aggregate(MealPlan plan) {
        Map<AggregateKey, AggregateValue> totals = new LinkedHashMap<>();
        for (RecipeEntity recipe : plan.getRecipes()) {
            for (Ingredient ingredient : recipe.getIngredients()) {
                UnitInfo unit = UnitInfo.forUnit(ingredient.unit());
                AggregateKey key = new AggregateKey(
                        ingredient.name().trim().toLowerCase(Locale.ROOT),
                        ingredient.category().trim().toLowerCase(Locale.ROOT),
                        unit.dimension());
                AggregateValue existing = totals.get(key);
                BigDecimal baseQuantity = ingredient.quantity().multiply(unit.toBase());
                if (existing == null) {
                    totals.put(key, new AggregateValue(ingredient.name().trim(), ingredient.category().trim(),
                            baseQuantity, unit));
                } else {
                    totals.put(key, new AggregateValue(existing.name(), existing.category(),
                            existing.baseQuantity().add(baseQuantity), existing.unitInfo()));
                }
            }
        }
        return totals.values().stream()
                .map(AggregateValue::toItem)
                .sorted(Comparator.comparing(AggregatedItem::category).thenComparing(AggregatedItem::name))
                .toList();
    }

    record AggregatedItem(String name, String category, BigDecimal quantity, String unit) {
    }

    private record AggregateKey(String name, String category, String dimension) {
    }

    private record AggregateValue(String name, String category, BigDecimal baseQuantity, UnitInfo unitInfo) {
        AggregatedItem toItem() {
            BigDecimal quantity = baseQuantity;
            String unit = unitInfo.baseUnit();
            if ("mass".equals(unitInfo.dimension()) && quantity.compareTo(BigDecimal.valueOf(1000)) >= 0) {
                quantity = quantity.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
                unit = "kg";
            } else if ("volume".equals(unitInfo.dimension()) && quantity.compareTo(BigDecimal.valueOf(1000)) >= 0) {
                quantity = quantity.divide(BigDecimal.valueOf(1000), 3, RoundingMode.HALF_UP);
                unit = "l";
            }
            return new AggregatedItem(name, category, quantity.setScale(3, RoundingMode.HALF_UP).stripTrailingZeros(), unit);
        }
    }

    private record UnitInfo(String dimension, String baseUnit, BigDecimal toBase) {
        static UnitInfo forUnit(String value) {
            String unit = value.trim().toLowerCase(Locale.ROOT);
            return switch (unit) {
                case "g", "gram", "grams" -> new UnitInfo("mass", "g", BigDecimal.ONE);
                case "kg", "kilogram", "kilograms" -> new UnitInfo("mass", "g", BigDecimal.valueOf(1000));
                case "ml", "milliliter", "milliliters", "millilitre", "millilitres" -> new UnitInfo("volume", "ml", BigDecimal.ONE);
                case "l", "liter", "liters", "litre", "litres" -> new UnitInfo("volume", "ml", BigDecimal.valueOf(1000));
                case "tsp", "teaspoon", "teaspoons" -> new UnitInfo("volume", "ml", BigDecimal.valueOf(5));
                case "tbsp", "tablespoon", "tablespoons" -> new UnitInfo("volume", "ml", BigDecimal.valueOf(15));
                case "cup", "cups" -> new UnitInfo("volume", "ml", BigDecimal.valueOf(240));
                case "pc", "pcs", "piece", "pieces", "unit", "units" -> new UnitInfo("count", "piece", BigDecimal.ONE);
                default -> new UnitInfo("unit:" + unit, unit, BigDecimal.ONE);
            };
        }
    }
}