package com.Moventia.auth.controller;

import com.Moventia.auth.dto.AuthResponse;
import com.Moventia.auth.dto.LoginRequest;
import com.Moventia.auth.dto.SignupRequest;
import com.Moventia.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST endpoints consumed by the CineReview React frontend.
 *
 *  POST /api/auth/signup  →  create account
 *  POST /api/auth/login   →  sign in
 *
 * All business logic is delegated to AuthService.
 * This class only handles HTTP concerns (status codes, error shaping).
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "${cinereview.cors.allowed-origins:http://localhost:5173}")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ── POST /api/auth/signup ─────────────────────────────────────────────────

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@Valid @RequestBody SignupRequest request) {
        try {
            AuthResponse response = authService.signup(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException ex) {
            // Email or username already taken
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    // ── POST /api/auth/login ──────────────────────────────────────────────────

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException ex) {
            // Wrong email/password — always return 401, never reveal which field is wrong
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid email or password."));
        }
    }
}
