package com.Moventia.profile.controller;

import com.Moventia.profile.dto.ProfileResponse;
import com.Moventia.profile.dto.UpdateProfileRequest;
import com.Moventia.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // ── GET /api/profile/search?q= ──────────────────────────────────────────
    @GetMapping("/search")
    public ResponseEntity<List<ProfileResponse>> searchUsers(@RequestParam("q") String query) {
        return ResponseEntity.ok(profileService.searchUsers(query));
    }

    // ── GET /api/profile/me ──────────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(profileService.getMyProfile(email));
    }

    // ── GET /api/profile/{username} ──────────────────────────────────────────
    @GetMapping("/{username}")
    public ResponseEntity<ProfileResponse> getProfile(
            @PathVariable String username,
            Authentication authentication
    ) {
        String requestingEmail = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(profileService.getProfileByUsername(username, requestingEmail));
    }

    // ── PUT /api/profile/me ──────────────────────────────────────────────────
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(profileService.updateMyProfile(email, request));
    }

    // ── POST /api/profile/me/avatar ─────────────────────────────────────────
    @PostMapping("/me/avatar")
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            Authentication authentication
    ) throws IOException {
        String avatarUrl = profileService.uploadAvatar(authentication.getName(), file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }

    // ── GET /api/profile/{username}/avatar ──────────────────────────────────
    @GetMapping("/{username}/avatar")
    public ResponseEntity<byte[]> getAvatar(@PathVariable String username) {
        com.Moventia.auth.model.User user = profileService.getUserEntity(username);
        if (user.getAvatar() == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, user.getAvatarContentType())
                .body(user.getAvatar());
    }

    // ── GET /api/profile/{username}/followers ────────────────────────────────
    @GetMapping("/{username}/followers")
    public ResponseEntity<List<ProfileResponse>> getFollowers(
            @PathVariable String username,
            Authentication authentication
    ) {
        String email = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(profileService.getFollowers(username, email));
    }

    // ── GET /api/profile/{username}/following ────────────────────────────────
    @GetMapping("/{username}/following")
    public ResponseEntity<List<ProfileResponse>> getFollowing(
            @PathVariable String username,
            Authentication authentication
    ) {
        String email = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(profileService.getFollowing(username, email));
    }

    // ── POST /api/profile/{username}/follow ─────────────────────────────────
    @PostMapping("/{username}/follow")
    public ResponseEntity<Void> followUser(
            @PathVariable String username,
            Authentication authentication
    ) {
        profileService.followUser(authentication.getName(), username);
        return ResponseEntity.ok().build();
    }

    // ── DELETE /api/profile/{username}/follow ────────────────────────────────
    @DeleteMapping("/{username}/follow")
    public ResponseEntity<Void> unfollowUser(
            @PathVariable String username,
            Authentication authentication
    ) {
        profileService.unfollowUser(authentication.getName(), username);
        return ResponseEntity.ok().build();
    }
}
