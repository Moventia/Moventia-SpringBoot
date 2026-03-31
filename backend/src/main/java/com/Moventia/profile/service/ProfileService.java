package com.Moventia.profile.service;

import com.Moventia.auth.model.User;
import com.Moventia.auth.repository.UserRepository;
import com.Moventia.profile.dto.ProfileResponse;
import com.Moventia.profile.dto.UpdateProfileRequest;
import com.Moventia.profile.model.Follow;
import com.Moventia.profile.repository.FollowRepository;
import com.Moventia.notification.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final NotificationService notificationService;

    public ProfileService(UserRepository userRepository,
                          FollowRepository followRepository,
                          NotificationService notificationService) {
        this.userRepository = userRepository;
        this.followRepository = followRepository;
        this.notificationService = notificationService;
    }

    // ── GET /api/profile/{username} ──────────────────────────────────────────
    public ProfileResponse getProfileByUsername(String username, String requestingEmail) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        boolean isOwn = requestingEmail != null && requestingEmail.equals(user.getEmail());
        boolean followedByMe = false;
        if (requestingEmail != null && !isOwn) {
            User requester = userRepository.findByEmail(requestingEmail).orElse(null);
            if (requester != null) {
                followedByMe = followRepository.existsByFollowerIdAndFollowingId(requester.getId(), user.getId());
            }
        }
        return buildResponse(user, isOwn, followedByMe);
    }

    // ── GET /api/profile/me ──────────────────────────────────────────────────
    public ProfileResponse getMyProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return buildResponse(user, true, false);
    }

    // ── PUT /api/profile/me ──────────────────────────────────────────────────
    public ProfileResponse updateMyProfile(String email, UpdateProfileRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        // avatar is now updated via /api/profile/me/avatar endpoint only
        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        userRepository.save(user);
        return buildResponse(user, true, false);
    }

    // ── POST /api/profile/me/avatar ─────────────────────────────────────────
    public String uploadAvatar(String email, MultipartFile file) throws IOException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAvatar(file.getBytes());
        user.setAvatarContentType(file.getContentType());
        userRepository.save(user);

        return "http://localhost:8080/api/profile/" + user.getUsername() + "/avatar";
    }

    // ── GET /api/profile/{username}/avatar (Helper for Controller) ────────
    public User getUserEntity(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    // ── GET /api/profile/search?q= ──────────────────────────────────────────
    public List<ProfileResponse> searchUsers(String query) {
        List<User> users = userRepository
                .findByUsernameContainingIgnoreCaseOrFullNameContainingIgnoreCase(query, query);
        return users.stream()
                .map(u -> buildResponse(u, false, false))
                .collect(Collectors.toList());
    }

    // ── GET /api/profile/{username}/followers ────────────────────────────────
    public List<ProfileResponse> getFollowers(String username, String requestingEmail) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<Follow> follows = followRepository.findByFollowingId(user.getId());
        List<Long> followerIds = follows.stream().map(Follow::getFollowerId).collect(Collectors.toList());

        Long requesterId = null;
        if (requestingEmail != null) {
            User requester = userRepository.findByEmail(requestingEmail).orElse(null);
            if (requester != null) requesterId = requester.getId();
        }

        return buildUserList(followerIds, requesterId);
    }

    // ── GET /api/profile/{username}/following ────────────────────────────────
    public List<ProfileResponse> getFollowing(String username, String requestingEmail) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<Follow> follows = followRepository.findByFollowerId(user.getId());
        List<Long> followingIds = follows.stream().map(Follow::getFollowingId).collect(Collectors.toList());

        Long requesterId = null;
        if (requestingEmail != null) {
            User requester = userRepository.findByEmail(requestingEmail).orElse(null);
            if (requester != null) requesterId = requester.getId();
        }

        return buildUserList(followingIds, requesterId);
    }

    // ── POST /api/profile/{username}/follow ─────────────────────────────────
    @Transactional
    public void followUser(String email, String targetUsername) {
        User follower = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found: " + targetUsername));

        if (follower.getId().equals(target.getId())) {
            throw new RuntimeException("Cannot follow yourself");
        }
        if (followRepository.existsByFollowerIdAndFollowingId(follower.getId(), target.getId())) {
            return;
        }

        followRepository.save(Follow.builder()
                .followerId(follower.getId())
                .followingId(target.getId())
                .build());

        follower.setFollowingCount(follower.getFollowingCount() + 1);
        target.setFollowerCount(target.getFollowerCount() + 1);
        userRepository.save(follower);
        userRepository.save(target);

        // 🔔 Notify the person being followed
        notificationService.createFollowNotification(
                follower.getUsername(), target.getUsername());
    }

    // ── DELETE /api/profile/{username}/follow ────────────────────────────────
    @Transactional
    public void unfollowUser(String email, String targetUsername) {
        User follower = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        User target = userRepository.findByUsername(targetUsername)
                .orElseThrow(() -> new RuntimeException("Target user not found: " + targetUsername));

        if (!followRepository.existsByFollowerIdAndFollowingId(follower.getId(), target.getId())) {
            return;
        }

        followRepository.deleteByFollowerIdAndFollowingId(follower.getId(), target.getId());

        follower.setFollowingCount(Math.max(0, follower.getFollowingCount() - 1));
        target.setFollowerCount(Math.max(0, target.getFollowerCount() - 1));
        userRepository.save(follower);
        userRepository.save(target);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private List<ProfileResponse> buildUserList(List<Long> userIds, Long requesterId) {
        if (userIds.isEmpty()) return List.of();

        List<User> users = userRepository.findAllById(userIds);

        List<Long> followedIds = List.of();
        if (requesterId != null) {
            List<Follow> myFollows = followRepository.findByFollowerId(requesterId);
            followedIds = myFollows.stream().map(Follow::getFollowingId).collect(Collectors.toList());
        }

        List<Long> finalFollowedIds = followedIds;
        return users.stream()
                .map(u -> {
                    boolean isOwn = requesterId != null && requesterId.equals(u.getId());
                    boolean followed = finalFollowedIds.contains(u.getId());
                    return buildResponse(u, isOwn, followed);
                })
                .collect(Collectors.toList());
    }

    private ProfileResponse buildResponse(User user, boolean isOwnProfile, boolean isFollowedByMe) {
        String generatedAvatarUrl = user.getAvatar() != null ? "http://localhost:8080/api/profile/" + user.getUsername() + "/avatar" : null;

        return ProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(isOwnProfile ? user.getEmail() : null)
                .bio(user.getBio())
                .avatarUrl(generatedAvatarUrl)
                .followerCount(user.getFollowerCount())
                .followingCount(user.getFollowingCount())
                .reviewCount(user.getReviewCount())
                .isOwnProfile(isOwnProfile)
                .isFollowedByMe(isFollowedByMe)
                .build();
    }
}
