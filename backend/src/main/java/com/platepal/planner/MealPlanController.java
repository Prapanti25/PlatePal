package com.platepal.planner;

import java.security.Principal;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meal-plans")
public class MealPlanController {
    private final MealPlanService mealPlanService;

    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping("/generate")
    public MealPlanView generate(Principal principal) {
        return mealPlanService.generate(principal.getName());
    }

    @GetMapping("/active")
    public MealPlanView active(Principal principal) {
        return mealPlanService.active(principal.getName());
    }
}