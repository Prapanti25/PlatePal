package com.platepal.planner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.ObjectMapper;

class MealPlanGeneratorTest {
    @Test
    void loadsSevenDayFallbackWhenOpenAiKeyIsMissing() {
        ObjectMapper objectMapper = new ObjectMapper();
        MealPlanGenerator generator = new MealPlanGenerator(
                objectMapper,
                new MealPlanPromptBuilder(objectMapper),
                "",
                "gpt-4o-mini");

        GenerationResult result = generator.generate(null);

        assertTrue(result.fallbackUsed());
        assertEquals(7, result.plan().days().size());
        assertEquals(21, result.plan().days().stream().mapToInt(day -> day.meals().size()).sum());
        assertEquals(7, result.plan().days().stream().filter(day -> day.snack() != null).count());
    }
}