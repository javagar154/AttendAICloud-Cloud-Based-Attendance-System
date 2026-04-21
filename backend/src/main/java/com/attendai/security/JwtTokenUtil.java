package com.attendai.security;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.security.PublicKey;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Utility for validating AWS Cognito JWT tokens using the Cognito JWKS endpoint.
 * Tokens are signed by Cognito using RS256 — we fetch public keys from JWKS and verify.
 */
@Component
public class JwtTokenUtil {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenUtil.class);

    @Value("${aws.cognito.jwks-url}")
    private String jwksUrl;

    @Value("${aws.cognito.user-pool-id}")
    private String userPoolId;

    @Value("${aws.cognito.region}")
    private String region;

    @Value("${aws.dummy-mode:false}")
    private boolean dummyMode;

    private JwkProvider provider;

    private JwkProvider getProvider() {
        if (provider == null) {
            try {
                provider = new JwkProviderBuilder(new URL(jwksUrl))
                        .cached(10, 24, TimeUnit.HOURS)
                        .rateLimited(10, 1, TimeUnit.MINUTES)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException("Failed to build JWKS provider", e);
            }
        }
        return provider;
    }

    /**
     * Validate the Cognito JWT token and return its Claims if valid.
     *
     * @param token raw JWT string (without "Bearer " prefix)
     * @return Claims object containing username, email, etc.
     * @throws Exception if the token is invalid or expired
     */
    public Claims validateToken(String token) throws Exception {
        // Dummy mode check for local testing
        if (dummyMode || "fake_access_key".equals(System.getenv("AWS_ACCESS_KEY_ID"))) {
            log.info("[DUMMY MODE] Bypassing JWT validation");
            return Jwts.claims()
                    .subject("test-user")
                    .add("email", "admin@attendai.com")
                    .build();
        }

        // Decode header to get key id (kid)
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT format");
        }

        // Decode base64 header to get kid
        String headerJson = new String(java.util.Base64.getUrlDecoder().decode(parts[0]));
        String kid = headerJson.replaceAll(".*\"kid\":\"([^\"]+)\".*", "$1");

        // Fetch the public key from Cognito JWKS
        Jwk jwk = getProvider().get(kid);
        PublicKey publicKey = jwk.getPublicKey();

        // Parse and validate token
        Claims claims = Jwts.parser()
                .verifyWith((java.security.interfaces.RSAPublicKey) publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        // Verify the token is not expired
        if (claims.getExpiration().before(new Date())) {
            throw new IllegalArgumentException("JWT token is expired");
        }

        return claims;
    }

    /**
     * Extract username (sub or email) from a validated token.
     */
    public String getUsernameFromToken(String token) throws Exception {
        Claims claims = validateToken(token);
        // Cognito uses "email" or "cognito:username" as the user identifier
        String email = claims.get("email", String.class);
        return email != null ? email : claims.getSubject();
    }

    /**
     * Quick check: is the token structurally valid (does not check signature).
     */
    public boolean isTokenValid(String token) {
        try {
            validateToken(token);
            return true;
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }
}
