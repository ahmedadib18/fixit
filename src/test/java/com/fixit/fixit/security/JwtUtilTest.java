package com.fixit.fixit.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "testSecretKeyForJwtTokenGenerationAndValidation");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 86400000L);
    }

    @Test
    void generateToken_Success() {
        String token = jwtUtil.generateToken("test@example.com", 1L, "USER");

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void extractEmail_Success() {
        String token = jwtUtil.generateToken("test@example.com", 1L, "USER");

        String email = jwtUtil.extractEmail(token);

        assertEquals("test@example.com", email);
    }

    @Test
    void extractUserId_Success() {
        String token = jwtUtil.generateToken("test@example.com", 1L, "USER");

        Long userId = jwtUtil.extractUserId(token);

        assertEquals(1L, userId);
    }

    @Test
    void extractUserType_Success() {
        String token = jwtUtil.generateToken("test@example.com", 1L, "HELPER");

        String userType = jwtUtil.extractUserType(token);

        assertEquals("HELPER", userType);
    }

    @Test
    void validateToken_ValidToken_ReturnsTrue() {
        String token = jwtUtil.generateToken("test@example.com", 1L, "USER");

        boolean isValid = jwtUtil.validateToken(token, "test@example.com");

        assertTrue(isValid);
    }

    @Test
    void validateToken_WrongEmail_ReturnsFalse() {
        String token = jwtUtil.generateToken("test@example.com", 1L, "USER");

        boolean isValid = jwtUtil.validateToken(token, "wrong@example.com");

        assertFalse(isValid);
    }

    @Test
    void isTokenExpired_FreshToken_ReturnsFalse() {
        String token = jwtUtil.generateToken("test@example.com", 1L, "USER");

        // Token should not be expired immediately after generation
        assertDoesNotThrow(() -> jwtUtil.extractEmail(token));
    }
}
