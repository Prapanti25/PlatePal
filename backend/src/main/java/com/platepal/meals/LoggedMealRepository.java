package com.platepal.meals;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface LoggedMealRepository extends JpaRepository<LoggedMeal, Long> {
    List<LoggedMeal> findTop20ByUser_FirebaseUidOrderByLoggedAtDesc(String firebaseUid);

    List<LoggedMeal> findAllByUser_FirebaseUidAndLoggedAtGreaterThanEqualOrderByLoggedAtDesc(
            String firebaseUid,
            Instant startOfDay);
}