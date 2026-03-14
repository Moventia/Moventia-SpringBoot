package com.Moventia.profile.service;

import com.Moventia.auth.model.User;
import com.Moventia.auth.repository.UserRepository;
import com.Moventia.profile.dto.ProfileResponse;
import com.Moventia.profile.dto.UpdateProfileRequest;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;

    public ProfileService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // ── GET /api/profile/{username} ──────────────────────────────────────────
    // Public — anyone can view a profile
    // requestingEmail = null if the caller is not authenticated
    public ProfileResponse getProfileByUsername(String username, String requestingEmail) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        boolean isOwn = requestingEmail != null && requestingEmail.equals(user.getEmail());
        return buildResponse(user, isOwn);
    }

    // ── GET /api/profile/me ──────────────────────────────────────────────────
    // Authenticated — returns own profile with email included
    public ProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildResponse(user, true);
    }

    // ── PUT /api/profile/me ──────────────────────────────────────────────────
    // Authenticated — update bio, avatarUrl, fullName
    public ProfileResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        userRepository.save(user);
        return buildResponse(user, true);
    }

    // ── Helper ───────────────────────────────────────────────────────────────
    private ProfileResponse buildResponse(User user, boolean isOwnProfile) {
        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(isOwnProfile ? user.getEmail() : null)  // hide email on public view
                .bio(user.getBio())
                .avatarUrl(user.getAvatarUrl())
                .followerCount(user.getFollowerCount())
                .followingCount(user.getFollowingCount())
                .reviewCount(user.getReviewCount())
                .isOwnProfile(isOwnProfile)
                .build();
    }
}
