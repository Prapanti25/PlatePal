package com.platepal.profile;

import java.math.BigDecimal;
import java.util.List;

public record ProfileResponse(
        Long id,
        String firebaseUid,
        Integer calorieTarget,
        Integer householdSize,
        BigDecimal weeklyBudgetBdt,
        List<String> dietaryTags) {
    static ProfileResponse from(HealthProfile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUser().getFirebaseUid(),
                profile.getCalorieTarget(),
                profile.getHouseholdSize(),
                profile.getWeeklyBudgetBdt(),
                List.copyOf(profile.getDietaryTags()));
    }
}