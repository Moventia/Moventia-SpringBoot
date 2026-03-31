package com.Moventia.notification.controller;

import com.Moventia.notification.dto.NotificationResponse;
import com.Moventia.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    /**
     * GET /api/notifications?filter=all|unread|read
     *
     * The frontend tabs map directly to this query param:
     *   "All"    → ?filter=all    (default)
     *   "Unread" → ?filter=unread
     *   "Read"   → ?filter=read
     */
    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getNotifications(
            @RequestParam(defaultValue = "all") String filter,
            Authentication authentication
    ) {
        String email = authentication.getName();
        List<NotificationResponse> notifications = switch (filter.toLowerCase()) {
            case "unread" -> notificationService.getUnreadNotifications(email);
            case "read"   -> notificationService.getReadNotifications(email);
            default       -> notificationService.getAllNotifications(email);
        };
        return ResponseEntity.ok(notifications);
    }

    /**
     * GET /api/notifications/count
     *
     * Returns the unread badge count.
     * Call this on page load / periodically to update the bell icon.
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Authentication authentication) {
        long count = notificationService.getUnreadCount(authentication.getName());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    /**
     * POST /api/notifications/mark-read
     *
     * Call this when the user opens the notification panel.
     * Marks all currently-unread notifications as read in one batch.
     */
    @PostMapping("/mark-read")
    public ResponseEntity<Void> markAllRead(Authentication authentication) {
        notificationService.markAllRead(authentication.getName());
        return ResponseEntity.ok().build();
    }
}
