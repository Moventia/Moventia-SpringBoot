package com.Moventia.profile.repository;

import com.Moventia.profile.model.Follow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {

    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // All users who follow this user (their followingId == userId)
    List<Follow> findByFollowingId(Long followingId);

    // All users this user follows (their followerId == userId)
    List<Follow> findByFollowerId(Long followerId);
}
