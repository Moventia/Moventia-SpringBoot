package com.Moventia.profile.controller;

import com.Moventia.profile.dto.ProfileResponse;
import com.Moventia.profile.dto.UpdateProfileRequest;
import com.Moventia.profile.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    // ── GET /api/profile/me ──────────────────────────────────────────────────
    // Requires JWT. Returns the logged-in user's own profile.
    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Authentication authentication) {
        String email = authentication.getName();  // JwtAuthFilter sets email as principal
        return ResponseEntity.ok(profileService.getMyProfile(email));
    }

    // ── GET /api/profile/{username} ──────────────────────────────────────────
    // Public. Anyone can view. If caller is authenticated, isOwnProfile is set correctly.
    @GetMapping("/{username}")
    public ResponseEntity<ProfileResponse> getProfile(
            @PathVariable String username,
            Authentication authentication  // null if not logged in (Spring injects null for public routes)
    ) {
        String requestingEmail = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(profileService.getProfileByUsername(username, requestingEmail));
    }

    // ── PUT /api/profile/me ──────────────────────────────────────────────────
    // Requires JWT. Updates bio, avatarUrl, fullName.
    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateMyProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication
    ) {
        String email = authentication.getName();
        return ResponseEntity.ok(profileService.updateMyProfile(email, request));
    }
}
