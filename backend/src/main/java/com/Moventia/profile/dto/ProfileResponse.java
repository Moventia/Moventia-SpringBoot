package com.Moventia.profile.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProfileResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;       // only returned for own profile
    private String bio;
    private String avatarUrl;
    private int followerCount;
    private int followingCount;
    private int reviewCount;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isOwnProfile")
    private boolean isOwnProfile;
    
    @com.fasterxml.jackson.annotation.JsonProperty("isFollowedByMe")
    private boolean isFollowedByMe;
}

