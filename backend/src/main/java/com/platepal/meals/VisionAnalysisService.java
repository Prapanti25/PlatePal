package com.platepal.meals;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class VisionAnalysisService {
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String model;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    public VisionAnalysisService(
            ObjectMapper objectMapper,
            @Value("${openai.api-key:}") String apiKey,
            @Value("${openai.model:gpt-4o-mini}") String model) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.model = model;
    }

    public VisionAnalysis analyze(String imageUrl) {
        validateCloudinaryUrl(imageUrl);
        if (!StringUtils.hasText(apiKey)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "OpenAI API key is not configured");
        }

        try {
            List<Map<String, Object>> content = List.of(
                    Map.of("type", "text", "text", "Identify the dish and estimate calories and protein, carbs, and fats in grams for the visible serving. Return only JSON with dishName, estimatedCalories, proteinGrams, carbsGrams, fatsGrams. State estimates, not medical advice."),
                    Map.of("type", "image_url", "image_url", Map.of("url", imageUrl)));
            String body = objectMapper.writeValueAsString(Map.of(
                    "model", model,
                    "response_format", Map.of("type", "json_object"),
                    "messages", List.of(Map.of("role", "user", "content", content))));
            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.openai.com/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(15))
                    .header("Authorization", "Bearer " + apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI Vision could not analyze this image");
            }
            JsonNode contentNode = objectMapper.readTree(response.body()).path("choices").path(0)
                    .path("message").path("content");
            VisionAnalysis analysis = objectMapper.readValue(contentNode.asText(), VisionAnalysis.class);
            validateAnalysis(analysis);
            return analysis;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI Vision request was interrupted");
        } catch (IOException | IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI Vision returned an invalid response");
        }
    }

    private void validateCloudinaryUrl(String imageUrl) {
        try {
            URI uri = URI.create(imageUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme()) || uri.getHost() == null
                    || !"res.cloudinary.com".equalsIgnoreCase(uri.getHost())) {
                throw new IllegalArgumentException("Image URL must be a Cloudinary HTTPS URL");
            }
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image URL must be a valid Cloudinary HTTPS URL");
        }
    }

    private void validateAnalysis(VisionAnalysis analysis) {
        if (analysis == null || !StringUtils.hasText(analysis.dishName()) || analysis.estimatedCalories() == null
                || analysis.estimatedCalories() < 0 || !validMacro(analysis.proteinGrams())
                || !validMacro(analysis.carbsGrams()) || !validMacro(analysis.fatsGrams())) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "OpenAI Vision returned incomplete nutrition data");
        }
    }

    private boolean validMacro(BigDecimal value) {
        return value != null && value.signum() >= 0;
    }
}