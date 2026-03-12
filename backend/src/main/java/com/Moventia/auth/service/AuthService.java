package com.Moventia.auth.service;

import com.Moventia.auth.dto.AuthResponse;
import com.Moventia.auth.dto.LoginRequest;
import com.Moventia.auth.dto.SignupRequest;
import com.Moventia.auth.model.User;
import com.Moventia.auth.repository.UserRepository;
import com.Moventia.auth.security.JwtUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Core authentication logic — all heavy lifting lives here, not in the controller.
 *
 *  signup()  →  validate uniqueness → hash password → save → return JWT
 *  login()   →  find user by email → verify BCrypt hash → return JWT
 */
@Service
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;   // BCryptPasswordEncoder (wired in SecurityConfig)
    private final JwtUtils        jwtUtils;

    public AuthService(UserRepository  userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils        jwtUtils) {
        this.userRepository  = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils        = jwtUtils;
    }

    // ── Sign Up ───────────────────────────────────────────────────────────────

    @Transactional
    public AuthResponse signup(SignupRequest req) {

        // 1. Uniqueness checks
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email is already registered.");
        }
        if (userRepository.existsByUsername(req.getUsername())) {
            throw new IllegalArgumentException("Username is already taken.");
        }

        // 2. Hash the plain-text password with BCrypt (strength 12)
        String hashedPassword = passwordEncoder.encode(req.getPassword());

        // 3. Persist the new user
        User user = new User(
                req.getFullName(),
                req.getUsername(),
                req.getEmail(),
                hashedPassword
        );
        userRepository.save(user);

        // 4. Issue a JWT and return a safe response (no password hash exposed)
        String token = jwtUtils.generateToken(user.getEmail());
        return buildResponse(token, user);
    }

    // ── Sign In ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest req) {

        // 1. Look up user by email
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() ->
                        new BadCredentialsException("Invalid email or password."));

        // 2. BCrypt compare — passwordEncoder.matches() handles the salt automatically
        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        if (!user.isActive()) {
            throw new BadCredentialsException("Account is disabled.");
        }

        // 3. Issue JWT
        String token = jwtUtils.generateToken(user.getEmail());
        return buildResponse(token, user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AuthResponse buildResponse(String token, User user) {
        return new AuthResponse(
                token,
                user.getEmail(),
                user.getUsername(),
                user.getFullName(),
                "Login successful"
        );
    }
}
