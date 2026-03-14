package com.Moventia.profile.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileRequest {

    @Size(max = 500, message = "Bio must be 500 characters or less")
    private String bio;

    private String avatarUrl;

    // fullName can also be updated
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;
}
