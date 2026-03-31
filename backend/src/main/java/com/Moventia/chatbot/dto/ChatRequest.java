package com.Moventia.chatbot.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class ChatRequest {

    @NotBlank(message = "Message cannot be empty")
    private String message;

    // Full conversation history from frontend
    // Frontend maintains this, sends it with every request
    private List<ChatMessage> history;

    // User's reviewed movies — fetched once when chatbot opens
    // List of "Movie Title: X/5" strings built on frontend
    private List<WatchedMovie> watchedMovies;

    @Data
    public static class WatchedMovie {
        private String title;
        private int rating;        // 1-5
        private String reviewTitle; // one line summary of their review
    }
}
