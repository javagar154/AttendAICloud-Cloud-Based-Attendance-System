package com.attendai.controller;

import com.attendai.model.ApiResponse;
import com.attendai.service.CognitoAuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AuthenticationResultType;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller.
 * POST /api/auth/login  — Authenticate user via Cognito, return JWT tokens.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private CognitoAuthService cognitoAuthService;

    /**
     * Login endpoint — authenticates with Cognito and returns ID, access, and refresh tokens.
     * The ID token (JWT) is what the frontend stores and sends in the Authorization header.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<Map<String, String>>> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthenticationResultType result = cognitoAuthService.authenticate(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );

        Map<String, String> tokens = new HashMap<>();
        tokens.put("idToken", result.idToken());           // Use this as Bearer token
        tokens.put("accessToken", result.accessToken());
        tokens.put("refreshToken", result.refreshToken());
        tokens.put("tokenType", result.tokenType());
        tokens.put("expiresIn", String.valueOf(result.expiresIn()));

        return ResponseEntity.ok(ApiResponse.ok("Authentication successful", tokens));
    }

    // ─── Inner request DTO ───────────────────────────────────────────────────

    public static class LoginRequest {
        @Email(message = "Must be a valid email")
        @NotBlank(message = "Email is required")
        private String email;

        @NotBlank(message = "Password is required")
        private String password;
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
