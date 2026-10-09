package com.platepal.planner;

import java.util.List;

public record PlanDayDraft(int dayNumber, List<RecipeDraft> meals, RecipeDraft snack) {
}