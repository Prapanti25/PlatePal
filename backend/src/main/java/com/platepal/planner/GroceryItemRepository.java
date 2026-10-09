package com.platepal.planner;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface GroceryItemRepository extends JpaRepository<GroceryItem, Long> {
    List<GroceryItem> findAllByMealPlan_IdOrderByCategoryAscNameAsc(Long mealPlanId);

    Optional<GroceryItem> findByIdAndMealPlan_User_FirebaseUid(Long id, String firebaseUid);

    void deleteAllByMealPlan_Id(Long mealPlanId);
}