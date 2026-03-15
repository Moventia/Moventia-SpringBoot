package com.Moventia.review.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {

    private Long id;

    // User info
    private String username;
    private String userFullName;
    private String userAvatarUrl;

    // Movie info — enough to render review card without extra fetch
    private Integer tmdbId;
    private String movieTitle;
    private String moviePosterUrl;

    // Review content
    private int rating;
    private String title;
    private String content;
    private boolean hasSpoilers;
    private int likeCount;
    private String createdAt;      // formatted: "March 14, 2026"

    // Viewer context
    @com.fasterxml.jackson.annotation.JsonProperty("isLikedByMe")
    private boolean isLikedByMe;   // true if the requesting user liked this review

    @com.fasterxml.jackson.annotation.JsonProperty("isOwnReview")
    private boolean isOwnReview;   // true if the requesting user wrote this review
}
