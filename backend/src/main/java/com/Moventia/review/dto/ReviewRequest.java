package com.Moventia.review.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ReviewRequest {

    @NotNull(message = "tmdbId is required")
    private Integer tmdbId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @NotBlank(message = "Review title is required")
    @Size(max = 150, message = "Title must be 150 characters or less")
    private String title;

    @NotBlank(message = "Review content is required")
    @Size(min = 10, message = "Review must be at least 10 characters")
    private String content;

    private boolean hasSpoilers;
}
