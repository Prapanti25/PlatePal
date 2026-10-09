package com.platepal.planner;

import java.math.BigDecimal;

public record Ingredient(String name, String category, BigDecimal quantity, String unit) {
}