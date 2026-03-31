package com.Moventia.notification.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who receives this notification. */
    @Column(name = "recipient_username", nullable = false)
    private String recipientUsername;

    /** The user who triggered the action. */
    @Column(name = "actor_username", nullable = false)
    private String actorUsername;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /**
     * Context data — nullable, depends on type:
     *  FOLLOW       → null
     *  REVIEW_LIKE  → reviewId (as String)
     */
    @Column(name = "reference_id")
    private String referenceId;

    /** Human-readable message, e.g. "alice liked your review of Interstellar" */
    @Column(nullable = false)
    private String message;

    /**
     * false  = notification has NOT been seen yet  (shows up in "Unread" tab)
     * true   = user has opened the notification panel and seen it
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean read = false;

    @Builder.Default
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
