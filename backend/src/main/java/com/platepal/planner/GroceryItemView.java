package com.platepal.planner;

import java.math.BigDecimal;

public record GroceryItemView(
        Long id,
        String name,
        String category,
        BigDecimal quantity,
        String unit,
        boolean inPantry,
        boolean checked) {
    static GroceryItemView from(GroceryItem item) {
        return new GroceryItemView(item.getId(), item.getName(), item.getCategory(), item.getQuantity(),
                item.getUnit(), item.isInPantry(), item.isChecked());
    }
}