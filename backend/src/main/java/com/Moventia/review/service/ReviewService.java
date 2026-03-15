package com.Moventia.review.service;

import com.Moventia.auth.model.User;
import com.Moventia.auth.repository.UserRepository;
import com.Moventia.movie.model.Movie;
import com.Moventia.movie.repository.MovieRepository;
import com.Moventia.movie.service.MovieService;
import com.Moventia.profile.model.Follow;
import com.Moventia.profile.repository.FollowRepository;
import com.Moventia.review.dto.ReviewRequest;
import com.Moventia.review.dto.ReviewResponse;
import com.Moventia.review.model.Review;
import com.Moventia.review.model.ReviewLike;
import com.Moventia.review.repository.ReviewLikeRepository;
import com.Moventia.review.repository.ReviewRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final MovieService movieService;
    private final FollowRepository followRepository;

    private static final DateTimeFormatter DISPLAY_FORMAT =
            DateTimeFormatter.ofPattern("MMMM d, yyyy");

    public ReviewService(ReviewRepository reviewRepository,
                         ReviewLikeRepository reviewLikeRepository,
                         UserRepository userRepository,
                         MovieRepository movieRepository,
                         MovieService movieService,
                         FollowRepository followRepository) {
        this.reviewRepository = reviewRepository;
        this.reviewLikeRepository = reviewLikeRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.movieService = movieService;
        this.followRepository = followRepository;
    }

    // ── POST /api/reviews ────────────────────────────────────────────────────
    @Transactional
    public ReviewResponse submitReview(String email, ReviewRequest request) {
        User user = getUser(email);

        // Block duplicate review (user already reviewed this movie)
        if (reviewRepository.existsByUsernameAndMovieTmdbId(user.getUsername(), request.getTmdbId())) {
            throw new RuntimeException("You have already reviewed this movie. Delete your existing review first.");
        }

        // Cache-aside — ensures movie exists in our DB
        Movie movie = movieService.getOrFetchMovie(request.getTmdbId());

        Review review = Review.builder()
                .username(user.getUsername())
                .movie(movie)
                .rating(request.getRating())
                .title(request.getTitle())
                .content(request.getContent())
                .hasSpoilers(request.isHasSpoilers())
                .build();

        reviewRepository.save(review);

        // Update movie stats
        updateMovieStats(movie);

        // Increment user's review count
        user.setReviewCount(user.getReviewCount() + 1);
        userRepository.save(user);

        return buildResponse(review, user, false, false);
    }

    // ── GET /api/reviews/movie/{tmdbId} ──────────────────────────────────────
    public List<ReviewResponse> getMovieReviews(Integer tmdbId, String requestingEmail) {
        List<Review> reviews = reviewRepository.findByMovieTmdbIdOrderByCreatedAtDesc(tmdbId);
        String requestingUsername = getUsernameFromEmail(requestingEmail);
        return reviews.stream()
                .map(r -> buildResponseWithContext(r, requestingUsername))
                .collect(Collectors.toList());
    }

    // ── GET /api/reviews/user/{username} ─────────────────────────────────────
    public List<ReviewResponse> getUserReviews(String username, String requestingEmail) {
        List<Review> reviews = reviewRepository.findByUsernameOrderByCreatedAtDesc(username);
        String requestingUsername = getUsernameFromEmail(requestingEmail);
        return reviews.stream()
                .map(r -> buildResponseWithContext(r, requestingUsername))
                .collect(Collectors.toList());
    }

    // ── DELETE /api/reviews/{id} ─────────────────────────────────────────────
    @Transactional
    public void deleteReview(Long reviewId, String email) {
        User user = getUser(email);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!review.getUsername().equals(user.getUsername())) {
            throw new RuntimeException("You can only delete your own reviews");
        }

        Movie movie = review.getMovie();
        reviewRepository.delete(review);

        // Update movie stats
        updateMovieStats(movie);

        // Decrement user review count
        user.setReviewCount(Math.max(0, user.getReviewCount() - 1));
        userRepository.save(user);
    }

    // ── POST /api/reviews/{id}/like ──────────────────────────────────────────
    @Transactional
    public void likeReview(Long reviewId, String email) {
        User user = getUser(email);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (reviewLikeRepository.existsByUsernameAndReviewId(user.getUsername(), reviewId)) {
            return; // already liked, do nothing
        }

        reviewLikeRepository.save(ReviewLike.builder()
                .username(user.getUsername())
                .review(review)
                .build());

        review.setLikeCount(review.getLikeCount() + 1);
        reviewRepository.save(review);
    }

    // ── DELETE /api/reviews/{id}/like ────────────────────────────────────────
    @Transactional
    public void unlikeReview(Long reviewId, String email) {
        User user = getUser(email);
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new RuntimeException("Review not found"));

        if (!reviewLikeRepository.existsByUsernameAndReviewId(user.getUsername(), reviewId)) {
            return; // not liked, do nothing
        }

        reviewLikeRepository.deleteByUsernameAndReviewId(user.getUsername(), reviewId);
        review.setLikeCount(Math.max(0, review.getLikeCount() - 1));
        reviewRepository.save(review);
    }

    // ── GET /api/feed ────────────────────────────────────────────────────────
    // Reviews from people the requesting user follows
    public List<ReviewResponse> getFeed(String email, int page, int size) {
        User user = getUser(email);
        Pageable pageable = PageRequest.of(page, size);

        // Step 1 — get IDs of users this person follows
        List<Long> followingIds = followRepository.findByFollowerId(user.getId())
                .stream()
                .map(Follow::getFollowingId)
                .collect(Collectors.toList());

        // No follows yet — return empty feed
        if (followingIds.isEmpty()) {
            return List.of();
        }

        // Step 2 — resolve those IDs to usernames
        List<String> followingUsernames = userRepository.findAllById(followingIds)
                .stream()
                .map(User::getUsername)
                .collect(Collectors.toList());

        // Step 3 — fetch reviews by those usernames
        Page<Review> feedPage = reviewRepository
                .findByUsernameInOrderByCreatedAtDesc(followingUsernames, pageable);

        return feedPage.getContent().stream()
                .map(r -> buildResponseWithContext(r, user.getUsername()))
                .collect(Collectors.toList());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private void updateMovieStats(Movie movie) {
        int count = reviewRepository.countByMovieTmdbId(movie.getTmdbId());
        movie.setReviewCount(count);

        if (count > 0) {
            List<Review> allReviews = reviewRepository
                    .findByMovieTmdbIdOrderByCreatedAtDesc(movie.getTmdbId());
            double avg = allReviews.stream()
                    .mapToInt(Review::getRating)
                    .average()
                    .orElse(0.0);
            movie.setAverageRating(Math.round(avg * 10.0) / 10.0);
        } else {
            movie.setAverageRating(0.0);
        }

        movieRepository.save(movie);
    }

    private ReviewResponse buildResponseWithContext(Review review, String requestingUsername) {
        boolean likedByMe = requestingUsername != null &&
                reviewLikeRepository.existsByUsernameAndReviewId(requestingUsername, review.getId());
        boolean ownReview = requestingUsername != null &&
                requestingUsername.equals(review.getUsername());

        User author = userRepository.findByUsername(review.getUsername()).orElse(null);
        return buildResponse(review, author, likedByMe, ownReview);
    }

    private ReviewResponse buildResponse(Review review, User author,
                                         boolean likedByMe, boolean ownReview) {
        String avatarUrl = (author != null && author.getAvatar() != null)
                ? "http://localhost:8080/api/profile/" + author.getUsername() + "/avatar"
                : null;

        Movie movie = review.getMovie();

        return ReviewResponse.builder()
                .id(review.getId())
                .username(review.getUsername())
                .userFullName(author != null ? author.getFullName() : review.getUsername())
                .userAvatarUrl(avatarUrl)
                .tmdbId(movie.getTmdbId())
                .movieTitle(movie.getTitle())
                .moviePosterUrl(movie.getPosterPath() != null
                        ? "https://image.tmdb.org/t/p/w500" + movie.getPosterPath()
                        : null)
                .rating(review.getRating())
                .title(review.getTitle())
                .content(review.getContent())
                .hasSpoilers(review.isHasSpoilers())
                .likeCount(review.getLikeCount())
                .createdAt(review.getCreatedAt().format(DISPLAY_FORMAT))
                .isLikedByMe(likedByMe)
                .isOwnReview(ownReview)
                .build();
    }

    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private String getUsernameFromEmail(String email) {
        if (email == null) return null;
        return userRepository.findByEmail(email)
                .map(User::getUsername)
                .orElse(null);
    }
}
