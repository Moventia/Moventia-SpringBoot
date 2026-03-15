package com.Moventia.review.repository;

import com.Moventia.review.model.ReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewLikeRepository extends JpaRepository<ReviewLike, Long> {

    boolean existsByUsernameAndReviewId(String username, Long reviewId);

    void deleteByUsernameAndReviewId(String username, Long reviewId);
}
