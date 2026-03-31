package com.Moventia.chatbot.controller;

import com.Moventia.chatbot.dto.ChatRequest;
import com.Moventia.chatbot.dto.ChatResponse;
import com.Moventia.chatbot.service.ChatService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // ── POST /api/chat ────────────────────────────────────────────────────────
    // JWT required — chat is personalized per user
    @PostMapping
    public ResponseEntity<?> chat(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication
    ) {
        try {
            ChatResponse response = chatService.chat(request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException ex) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Chat service unavailable. Please try again."));
        }
    }
}
