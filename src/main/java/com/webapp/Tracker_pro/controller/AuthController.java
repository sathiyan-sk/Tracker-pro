package com.webapp.Tracker_pro.controller;

import com.webapp.Tracker_pro.dto.AuthResponse;
import com.webapp.Tracker_pro.dto.LoginRequest;
import com.webapp.Tracker_pro.dto.RegisterRequest;
import com.webapp.Tracker_pro.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication endpoints.
 * Handles user registration and login requests.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    /**
     * Register a new user
     * POST /api/auth/register
     * 
     * @param request Registration request with user details
     * @return AuthResponse with JWT token and user information
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Authenticate user and generate JWT token
     * POST /api/auth/login
     * 
     * @param request Login request with email and password
     * @return AuthResponse with JWT token and user information
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Health check endpoint
     * GET /api/auth/health
     * 
     * @return Simple health check response
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }
}
