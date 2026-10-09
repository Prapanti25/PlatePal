package com.platepal.planner;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    Optional<MealPlan> findFirstByUser_FirebaseUidAndStatusOrderByStartsOnDesc(String firebaseUid, String status);

    List<MealPlan> findAllByUser_FirebaseUidAndStatus(String firebaseUid, String status);
}