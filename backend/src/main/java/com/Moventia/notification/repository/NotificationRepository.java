package com.Moventia.notification.repository;

import com.Moventia.notification.model.Notification;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // ── Purge old notifications ──────────────────────────────────────────────

    /** Delete all notifications created before the given cutoff. */
    void deleteByCreatedAtBefore(LocalDateTime cutoff);

    // ── Fetch queries ────────────────────────────────────────────────────────

    /** All notifications for a user, newest first. */
    List<Notification> findByRecipientUsernameOrderByCreatedAtDesc(
            String recipientUsername, Pageable pageable);

    /** Only unread notifications for a user. */
    List<Notification> findByRecipientUsernameAndReadFalseOrderByCreatedAtDesc(
            String recipientUsername);

    /** Only read notifications for a user. */
    List<Notification> findByRecipientUsernameAndReadTrueOrderByCreatedAtDesc(
            String recipientUsername, Pageable pageable);

    // ── Count query (for the badge) ──────────────────────────────────────────

    long countByRecipientUsernameAndReadFalse(String recipientUsername);

    // ── Mark-as-read ─────────────────────────────────────────────────────────

    /** Mark all unread notifications for a user as read in one UPDATE. */
    @Modifying
    @Query("UPDATE Notification n SET n.read = true " +
           "WHERE n.recipientUsername = :username AND n.read = false")
    void markAllReadForUser(@Param("username") String username);
}
