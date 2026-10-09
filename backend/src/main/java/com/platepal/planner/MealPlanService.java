package com.platepal.planner;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.platepal.profile.AppUser;
import com.platepal.profile.AppUserRepository;
import com.platepal.profile.ProfileResponse;
import com.platepal.profile.ProfileService;

@Service
public class MealPlanService {
    private final AppUserRepository userRepository;
    private final MealPlanRepository mealPlanRepository;
    private final ProfileService profileService;
    private final MealPlanGenerator generator;
    private final GroceryListService groceryListService;

    public MealPlanService(
            AppUserRepository userRepository,
            MealPlanRepository mealPlanRepository,
            ProfileService profileService,
            MealPlanGenerator generator,
            GroceryListService groceryListService) {
        this.userRepository = userRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.profileService = profileService;
        this.generator = generator;
        this.groceryListService = groceryListService;
    }

    @Transactional
    public MealPlanView generate(String firebaseUid) {
        ProfileResponse profile = profileService.find(firebaseUid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.PRECONDITION_FAILED,
                        "Complete your health profile before generating a meal plan"));
        AppUser user = userRepository.findByFirebaseUid(firebaseUid)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User profile not found"));

        GenerationResult generated = generator.generate(profile);
        PlanDraft planDraft = generated.fallbackUsed() ? scaleForHousehold(generated.plan(), profile.householdSize()) : generated.plan();
        LocalDate startsOn = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.MONDAY));
        mealPlanRepository.findAllByUser_FirebaseUidAndStatus(firebaseUid, "ACTIVE")
                .forEach(MealPlan::archive);

        MealPlan mealPlan = new MealPlan(user, startsOn, startsOn.plusDays(6), generated.fallbackUsed());
        for (PlanDayDraft day : planDraft.days()) {
            for (RecipeDraft recipe : day.meals()) {
                mealPlan.addRecipe(new RecipeEntity((short) day.dayNumber(), recipe));
            }
            mealPlan.addRecipe(new RecipeEntity((short) day.dayNumber(), day.snack()));
        }
        MealPlan savedPlan = mealPlanRepository.save(mealPlan);
        groceryListService.refreshForPlan(savedPlan);
        return MealPlanView.from(savedPlan);
    }

    @Transactional(readOnly = true)
    public MealPlanView active(String firebaseUid) {
        MealPlan plan = mealPlanRepository.findFirstByUser_FirebaseUidAndStatusOrderByStartsOnDesc(firebaseUid, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active meal plan"));
        return MealPlanView.from(plan);
    }

    private PlanDraft scaleForHousehold(PlanDraft plan, int householdSize) {
        BigDecimal multiplier = BigDecimal.valueOf(householdSize);
        List<PlanDayDraft> scaledDays = new ArrayList<>();
        for (PlanDayDraft day : plan.days()) {
            List<RecipeDraft> scaledMeals = day.meals().stream().map(recipe -> scale(recipe, multiplier)).toList();
            scaledDays.add(new PlanDayDraft(day.dayNumber(), scaledMeals, scale(day.snack(), multiplier)));
        }
        return new PlanDraft(scaledDays);
    }

    private RecipeDraft scale(RecipeDraft recipe, BigDecimal multiplier) {
        List<Ingredient> ingredients = recipe.ingredients().stream()
                .map(ingredient -> new Ingredient(ingredient.name(), ingredient.category(),
                        ingredient.quantity().multiply(multiplier), ingredient.unit()))
                .toList();
        return new RecipeDraft(recipe.mealType(), recipe.name(), recipe.prepMinutes(),
                recipe.estimatedCostBdt().multiply(multiplier), ingredients, recipe.instructions());
    }
}