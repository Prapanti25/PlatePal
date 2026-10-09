package com.platepal.planner;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class BrevoEmailService {
    private final ObjectMapper objectMapper;
    private final String apiKey;
    private final String senderEmail;
    private final String senderName;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();

    public BrevoEmailService(
            ObjectMapper objectMapper,
            @Value("${brevo.api-key:}") String apiKey,
            @Value("${brevo.sender-email:}") String senderEmail,
            @Value("${brevo.sender-name:PlatePal}") String senderName) {
        this.objectMapper = objectMapper;
        this.apiKey = apiKey;
        this.senderEmail = senderEmail;
        this.senderName = senderName;
    }

    public void sendShoppingList(String recipient, List<GroceryItemView> items) {
        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(senderEmail)) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                    "Brevo API key and sender email must be configured");
        }
        try {
            Map<String, Object> sender = Map.of("name", senderName, "email", senderEmail);
            Map<String, Object> message = new LinkedHashMap<>();
            message.put("sender", sender);
            message.put("to", List.of(Map.of("email", recipient)));
            message.put("subject", "Your PlatePal grocery list");
            message.put("htmlContent", renderHtml(items));

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .timeout(Duration.ofSeconds(8))
                    .header("api-key", apiKey)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(message)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Brevo rejected the shopping list email");
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Brevo email request was interrupted");
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Shopping list email could not be serialized", exception);
        } catch (IOException exception) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Could not reach Brevo email service");
        }
    }

    private String renderHtml(List<GroceryItemView> items) {
        StringBuilder html = new StringBuilder("<h1>PlatePal shopping list</h1><ul>");
        for (GroceryItemView item : items) {
            html.append("<li><strong>").append(escape(item.name())).append("</strong> - ")
                    .append(escape(item.quantity().stripTrailingZeros().toPlainString()))
                    .append(' ').append(escape(item.unit())).append(" (")
                    .append(escape(item.category())).append(")</li>");
        }
        return html.append("</ul>").toString();
    }

    private String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
}