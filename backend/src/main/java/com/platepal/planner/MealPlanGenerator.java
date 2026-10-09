package com.platepal.planner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.platepal.profile.ProfileResponse;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class MealPlanGenerator {
    private final ObjectMapper objectMapper;
    private final MealPlanPromptBuilder promptBuilder;
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;

    public MealPlanGenerator(
            ObjectMapper objectMapper,
            MealPlanPromptBuilder promptBuilder,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model) {
        this.objectMapper = objectMapper;
        this.promptBuilder = promptBuilder;
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
    }

    public GenerationResult generate(ProfileResponse profile) {
        if (!StringUtils.hasText(apiKey)) {
            return fallback();
        }
        try {
            String prompt = promptBuilder.build(profile);
            String requestBody = objectMapper.writeValueAsString(new OpenAiRequest(
                    model,
                    List.of(
                            new OpenAiMessage("system", "You create safe, budget-aware meal plans and return valid JSON."),
                            new OpenAiMessage("user", prompt)),
                    new ResponseFormat("json_object")));
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(5))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return fallback();
            }
            JsonNode content = objectMapper.readTree(response.body()).path("choices").path(0)
                    .path("message").path("content");
            if (!content.isTextual()) {
                return fallback();
            }
            PlanDraft plan = objectMapper.readValue(content.asText(), PlanDraft.class);
            validate(plan);
            return new GenerationResult(plan, false);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return fallback();
        } catch (IOException | RuntimeException exception) {
            return fallback();
        }
    }

    private GenerationResult fallback() {
        try {
            PlanDraft plan = objectMapper.readValue(new ClassPathResource("fallback_meal_plan.json").getInputStream(),
                    PlanDraft.class);
            validate(plan);
            return new GenerationResult(plan, true);
        } catch (IOException | RuntimeException exception) {
            throw new IllegalStateException("Fallback meal plan could not be loaded", exception);
        }
    }

    private void validate(PlanDraft plan) {
        if (plan.days() == null || plan.days().size() != 7) {
            throw new IllegalArgumentException("Meal plan must have seven days");
        }
        for (int index = 0; index < 7; index++) {
            PlanDayDraft day = plan.days().get(index);
            if (day.dayNumber() != index + 1 || day.meals() == null || day.meals().size() != 3
                    || day.snack() == null) {
                throw new IllegalArgumentException("Each day must have three meals and one snack");
            }
            List<String> expected = List.of("BREAKFAST", "LUNCH", "DINNER");
            for (int mealIndex = 0; mealIndex < expected.size(); mealIndex++) {
                validateRecipe(day.meals().get(mealIndex), expected.get(mealIndex));
            }
            validateRecipe(day.snack(), "SNACK");
        }
    }

    private void validateRecipe(RecipeDraft recipe, String expectedMealType) {
        if (recipe == null || !expectedMealType.equalsIgnoreCase(recipe.mealType())
                || !StringUtils.hasText(recipe.name()) || recipe.prepMinutes() == null
                || recipe.estimatedCostBdt() == null || recipe.ingredients() == null || recipe.instructions() == null) {
            throw new IllegalArgumentException("Meal plan contains an incomplete recipe");
        }
        for (Ingredient ingredient : recipe.ingredients()) {
            if (ingredient == null || !StringUtils.hasText(ingredient.name())
                    || !StringUtils.hasText(ingredient.category()) || ingredient.quantity() == null
                    || ingredient.quantity().signum() < 0 || !StringUtils.hasText(ingredient.unit())) {
                throw new IllegalArgumentException("Meal plan contains an invalid ingredient");
            }
        }
    }

    private record OpenAiRequest(String model, List<OpenAiMessage> messages, ResponseFormat response_format) {
    }

    private record OpenAiMessage(String role, String content) {
    }

    private record ResponseFormat(String type) {
    }
}