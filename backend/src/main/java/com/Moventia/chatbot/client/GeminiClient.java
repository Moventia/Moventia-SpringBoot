package com.Moventia.chatbot.client;

import com.Moventia.chatbot.dto.ChatMessage;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;

import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final RestTemplate restTemplate;
    private final String apiKey;
    private final String apiUrl;

    public GeminiClient(
            RestTemplate restTemplate,
            @Value("${gemini.api.key}") String apiKey
    ) {
        this.restTemplate = restTemplate;
        this.apiKey = apiKey;
        this.apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

    }

    /**
     * Sends message + full conversation history to Gemini.
     * systemPrompt is prepended as first user message since Gemini Flash
     * doesn't have a separate system role — we inject it as context.
     */
    public String chat(String systemPrompt, List<ChatMessage> history, String userMessage) {

        // Build contents array — system prompt + history + new message
        List<Map<String, Object>> contents = new java.util.ArrayList<>();

        // Inject system prompt as first user turn
        contents.add(Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", systemPrompt))
        ));

        // Add a model acknowledgment so Gemini understands context
        contents.add(Map.of(
            "role", "model",
            "parts", List.of(Map.of("text", "Understood. I'm ready to help with movie recommendations and questions."))
        ));

        // Add conversation history
        if (history != null) {
            for (ChatMessage msg : history) {
                contents.add(Map.of(
                    "role", msg.getRole(),
                    "parts", List.of(Map.of("text", msg.getContent()))
                ));
            }
        }

        // Add current user message
        contents.add(Map.of(
            "role", "user",
            "parts", List.of(Map.of("text", userMessage))
        ));

        // Build request body
        Map<String, Object> requestBody = Map.of(
            "contents", contents,
            "generationConfig", Map.of(
                "temperature", 0.8,
                "maxOutputTokens", 500   // keep responses concise for chat
            )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<GeminiResponse> response = restTemplate.exchange(
                apiUrl,
                HttpMethod.POST,
                entity,
                GeminiResponse.class
            );

            if (response.getBody() != null &&
                response.getBody().getCandidates() != null &&
                !response.getBody().getCandidates().isEmpty()) {

                return response.getBody()
                        .getCandidates()
                        .get(0)
                        .getContent()
                        .getParts()
                        .get(0)
                        .getText();
            }
        } catch (Exception e) {
            throw new RuntimeException("Gemini API call failed: " + e.getMessage());
        }

        return "Sorry, I couldn't process that request right now.";
    }

    // ── Gemini response mapping ───────────────────────────────────────────────

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class GeminiResponse {
        private List<Candidate> candidates;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Candidate {
        private Content content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Part {
        private String text;
    }
}
