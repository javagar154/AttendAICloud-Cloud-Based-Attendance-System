package com.attendai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Service for interacting with AWS Cognito for authentication.
 * Uses the USER_PASSWORD_AUTH flow to authenticate users
 * and returns the JWT ID token on success.
 */
@Service
public class CognitoAuthService {

    private static final Logger log = LoggerFactory.getLogger(CognitoAuthService.class);

    @Autowired
    private CognitoIdentityProviderClient cognitoClient;

    @Value("${aws.cognito.client-id}")
    private String clientId;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.dummy-mode:false}")
    private boolean dummyMode;

    /**
     * Authenticate a user using Cognito USER_PASSWORD_AUTH flow.
     *
     * @param email    user's email address
     * @param password user's password
     * @return AuthenticationResultType containing idToken, accessToken, refreshToken
     * @throws RuntimeException on authentication failure
     */
    public AuthenticationResultType authenticate(String email, String password) {
        if (dummyMode || "fake_access_key".equals(System.getenv("AWS_ACCESS_KEY_ID"))) {
            log.info("[DUMMY MODE] Mocking Cognito authentication for user: {}", email);
            return AuthenticationResultType.builder()
                    .idToken("dummy-jwt-token")
                    .accessToken("dummy-access-token")
                    .refreshToken("dummy-refresh-token")
                    .expiresIn(3600)
                    .tokenType("Bearer")
                    .build();
        }

        Map<String, String> authParams = new HashMap<>();
        authParams.put("USERNAME", email);
        authParams.put("PASSWORD", password);

        InitiateAuthRequest request = InitiateAuthRequest.builder()
                .authFlow(AuthFlowType.USER_PASSWORD_AUTH)
                .clientId(clientId)
                .authParameters(authParams)
                .build();

        try {
            InitiateAuthResponse response = cognitoClient.initiateAuth(request);
            log.info("User authenticated successfully: {}", email);
            return response.authenticationResult();
        } catch (NotAuthorizedException e) {
            log.warn("Authentication failed for user {}: {}", email, e.getMessage());
            throw new RuntimeException("Invalid email or password.");
        } catch (UserNotFoundException e) {
            throw new RuntimeException("User not found. Please sign up first.");
        } catch (UserNotConfirmedException e) {
            throw new RuntimeException("Account not confirmed. Please verify your email.");
        }
    }

    /**
     * Get user attributes (name, email, etc.) from Cognito using an access token.
     */
    public GetUserResponse getUserInfo(String accessToken) {
        GetUserRequest request = GetUserRequest.builder()
                .accessToken(accessToken)
                .build();
        return cognitoClient.getUser(request);
    }
}
