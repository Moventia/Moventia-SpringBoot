package com.Moventia.chatbot.service;

import com.Moventia.chatbot.client.GeminiClient;
import com.Moventia.chatbot.dto.ChatRequest;
import com.Moventia.chatbot.dto.ChatResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChatService {

    private final GeminiClient geminiClient;

    public ChatService(GeminiClient geminiClient) {
        this.geminiClient = geminiClient;
    }

    public ChatResponse chat(ChatRequest request) {
        boolean isRecommendation = isRecommendationRequest(request.getMessage());

        String systemPrompt = isRecommendation
                ? buildRecommendationPrompt(request)
                : buildGeneralPrompt();

        String response = geminiClient.chat(
                systemPrompt,
                request.getHistory(),
                request.getMessage()
        );

        return ChatResponse.builder()
                .response(response)
                .isRecommendation(isRecommendation)
                .build();
    }

    // ── Intent detection ─────────────────────────────────────────────────────
    private boolean isRecommendationRequest(String message) {
        String lower = message.toLowerCase();
        return lower.contains("recommend") ||
               lower.contains("suggest") ||
               lower.contains("what should i watch") ||
               lower.contains("what to watch") ||
               lower.contains("based on my taste") ||
               lower.contains("similar to") ||
               lower.contains("next movie") ||
               lower.contains("something to watch") ||
               lower.contains("movie for me") ||
               lower.contains("what's good");
    }

    // ── Recommendation prompt — uses watched movies as context ────────────────
    private String buildRecommendationPrompt(ChatRequest request) {
        StringBuilder prompt = new StringBuilder();

        prompt.append("You are a knowledgeable movie recommendation assistant on Moventia, ")
              .append("a movie review platform. Be conversational, friendly and concise.\n\n");

        // Inject watched movies context if available
        if (request.getWatchedMovies() != null && !request.getWatchedMovies().isEmpty()) {

            prompt.append("This user has watched and reviewed these movies:\n");
            for (ChatRequest.WatchedMovie movie : request.getWatchedMovies()) {
                prompt.append(String.format("- %s (rated %d/5): \"%s\"\n",
                        movie.getTitle(),
                        movie.getRating(),
                        movie.getReviewTitle()));
            }

            prompt.append("\nIMPORTANT RULES:\n");
            prompt.append("1. NEVER suggest any movie from the watched list above\n");
            prompt.append("2. Base your recommendations on their taste shown by ratings\n");
            prompt.append("   High ratings (4-5) = they loved it, find similar\n");
            prompt.append("   Low ratings (1-2) = they disliked it, avoid similar style\n");
            prompt.append("3. Don't mention that you're reading their watch history\n");
            prompt.append("4. Keep response concise — max 3 recommendations with brief reason\n");
            prompt.append("5. Include both Hollywood and Bollywood options where relevant\n");

        } else {
            // No watch history — general recommendations
            prompt.append("The user hasn't reviewed any movies yet. ")
                  .append("Give general popular recommendations.\n")
                  .append("Keep response concise — max 3 recommendations with brief reason.\n")
                  .append("Include both Hollywood and Bollywood options where relevant.\n");
        }

        return prompt.toString();
    }

    // ── General prompt — movie questions, facts, discussions ─────────────────
    private String buildGeneralPrompt() {
        return "You are a knowledgeable movie assistant on Moventia, a movie review platform. " +
               "Answer questions about movies, directors, actors, plots, and cinema in general. " +
               "Be conversational, friendly, and concise. " +
               "If someone asks about a specific movie keep the response focused and spoiler-free " +
               "unless they explicitly ask for spoilers. " +
               "Include both Hollywood and Bollywood knowledge.";
    }
}
