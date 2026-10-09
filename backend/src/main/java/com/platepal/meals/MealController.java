package com.platepal.meals;

import java.security.Principal;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/meals")
public class MealController {
    private final MealLoggingService mealLoggingService;

    public MealController(MealLoggingService mealLoggingService) {
        this.mealLoggingService = mealLoggingService;
    }

    @PostMapping("/log-image")
    public LoggedMealResponse logImage(Principal principal, @Valid @RequestBody LogImageRequest request) {
        return mealLoggingService.log(principal.getName(), request.imageUrl());
    }

    @GetMapping("/recent")
    public List<LoggedMealResponse> recent(Principal principal) {
        return mealLoggingService.recent(principal.getName());
    }

    @GetMapping("/today")
    public List<LoggedMealResponse> today(Principal principal) {
        return mealLoggingService.today(principal.getName());
    }
}