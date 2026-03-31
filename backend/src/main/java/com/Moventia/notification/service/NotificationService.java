package com.Moventia.notification.service;

import com.Moventia.auth.model.User;
import com.Moventia.auth.repository.UserRepository;
import com.Moventia.notification.dto.NotificationResponse;
import com.Moventia.notification.model.Notification;
import com.Moventia.notification.model.NotificationType;
import com.Moventia.notification.repository.NotificationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    private static final int DEFAULT_PAGE_SIZE = 30;

    public NotificationService(NotificationRepository notificationRepository,
                               UserRepository userRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
    }

    // ── Internal: create a FOLLOW notification ───────────────────────────────
    public void createFollowNotification(String actorUsername, String recipientUsername) {
        // Don't notify yourself
        if (actorUsername.equals(recipientUsername)) return;

        String message = actorUsername + " started following you";

        Notification notification = Notification.builder()
                .recipientUsername(recipientUsername)
                .actorUsername(actorUsername)
                .type(NotificationType.FOLLOW)
                .message(message)
                .build();

        notificationRepository.save(notification);
    }

    // ── Internal: create a REVIEW_LIKE notification ──────────────────────────
    public void createReviewLikeNotification(String actorUsername,
                                             String recipientUsername,
                                             Long reviewId,
                                             String movieTitle) {
        // Don't notify yourself
        if (actorUsername.equals(recipientUsername)) return;

        String message = actorUsername + " liked your review of " + movieTitle;

        Notification notification = Notification.builder()
                .recipientUsername(recipientUsername)
                .actorUsername(actorUsername)
                .type(NotificationType.REVIEW_LIKE)
                .referenceId(String.valueOf(reviewId))
                .message(message)
                .build();

        notificationRepository.save(notification);
    }

    // ── GET all notifications (paginated) ────────────────────────────────────
    public List<NotificationResponse> getAllNotifications(String email) {
        String username = resolveUsername(email);
        return notificationRepository
                .findByRecipientUsernameOrderByCreatedAtDesc(
                        username, PageRequest.of(0, DEFAULT_PAGE_SIZE))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── GET unread notifications ──────────────────────────────────────────────
    public List<NotificationResponse> getUnreadNotifications(String email) {
        String username = resolveUsername(email);
        return notificationRepository
                .findByRecipientUsernameAndReadFalseOrderByCreatedAtDesc(username)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── GET read notifications ────────────────────────────────────────────────
    public List<NotificationResponse> getReadNotifications(String email) {
        String username = resolveUsername(email);
        return notificationRepository
                .findByRecipientUsernameAndReadTrueOrderByCreatedAtDesc(
                        username, PageRequest.of(0, DEFAULT_PAGE_SIZE))
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── GET unread count (for the bell badge) ────────────────────────────────
    public long getUnreadCount(String email) {
        String username = resolveUsername(email);
        return notificationRepository.countByRecipientUsernameAndReadFalse(username);
    }

    // ── POST mark-all-read (called when user opens notification panel) ────────
    @Transactional
    public void markAllRead(String email) {
        String username = resolveUsername(email);
        notificationRepository.markAllReadForUser(username);
    }

    // ── Scheduled: delete notifications older than 7 days ────────────────────
    @Scheduled(cron = "0 0 0 * * *") // midnight every day
    @Transactional
    public void purgeOldNotifications() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteByCreatedAtBefore(cutoff);
        log.info("Purged notifications older than {}", cutoff);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private NotificationResponse toResponse(Notification n) {
        User actor = userRepository.findByUsername(n.getActorUsername()).orElse(null);
        String avatarUrl = (actor != null && actor.getAvatar() != null)
                ? "http://localhost:8080/api/profile/" + actor.getUsername() + "/avatar"
                : null;

        return NotificationResponse.builder()
                .id(n.getId())
                .actorUsername(n.getActorUsername())
                .actorAvatarUrl(avatarUrl)
                .type(n.getType())
                .referenceId(n.getReferenceId())
                .message(n.getMessage())
                .read(n.isRead())
                .createdAt(n.getCreatedAt().format(DISPLAY_FORMAT))
                .build();
    }

    private String resolveUsername(String email) {
        return userRepository.findByEmail(email)
                .map(User::getUsername)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
