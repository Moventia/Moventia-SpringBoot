package com.Moventia.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** Payload from the React signup form → POST /api/auth/signup */
@Data
public class SignupRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 20, message = "Username must be 3-20 characters")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Must be a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    public @NotBlank(message = "Full name is required") String getFullName() {
        return fullName;
    }

    public @NotBlank(message = "Username is required") @Size(min = 3, max = 20, message = "Username must be 3-20 characters") String getUsername() {
        return username;
    }

    public @NotBlank(message = "Email is required") @Email(message = "Must be a valid email address") String getEmail() {
        return email;
    }

    public @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String getPassword() {
        return password;
    }

    public void setFullName(@NotBlank(message = "Full name is required") String fullName) {
        this.fullName = fullName;
    }

    public void setUsername(@NotBlank(message = "Username is required") @Size(min = 3, max = 20, message = "Username must be 3-20 characters") String username) {
        this.username = username;
    }

    public void setEmail(@NotBlank(message = "Email is required") @Email(message = "Must be a valid email address") String email) {
        this.email = email;
    }

    public void setPassword(@NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password) {
        this.password = password;
    }
}
