package com.Moventia.notification.dto;

import com.Moventia.notification.model.NotificationType;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;
    private String actorUsername;
    private String actorAvatarUrl;   // resolved from actorUsername
    private NotificationType type;
    private String referenceId;
    private String message;
    private boolean read;
    private String createdAt;        // formatted for display
}
