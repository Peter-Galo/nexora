package com.nexora.security;

import com.nexora.model.Role;
import com.nexora.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;
    private static final String SECRET_KEY = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";
    private static final long EXPIRATION = 86400000; // 24 hours

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", EXPIRATION);

        // Create a test user
        testUser = new User();
        testUser.setUuid(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPassword("password");
        testUser.addRole(Role.USER);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        // Act
        String token = jwtService.generateToken(testUser);

        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertEquals("test@example.com", jwtService.extractUsername(token));
        assertTrue(jwtService.isTokenValid(token, testUser));
    }

    @Test
    void extractUsername_shouldReturnCorrectUsername() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String username = jwtService.extractUsername(token);

        // Assert
        assertEquals("test@example.com", username);
    }

    @Test
    void extractAuthorities_shouldReturnCorrectAuthorities() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        List<String> authorities = jwtService.extractAuthorities(token);

        // Assert
        assertNotNull(authorities);
        assertEquals(1, authorities.size());
        assertEquals(Role.USER.name(), authorities.get(0));
    }

    @Test
    void extractUuid_shouldReturnCorrectUuid() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        String uuidString = jwtService.extractUuid(token);

        // Assert
        assertNotNull(uuidString);
        assertEquals(testUser.getUuid().toString(), uuidString);
    }

    @Test
    void isTokenValid_shouldReturnTrueForValidToken() {
        // Arrange
        String token = jwtService.generateToken(testUser);

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertTrue(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalseForInvalidToken() {
        // Arrange
        String token = "invalid.token.string";

        // Act
        boolean isValid = jwtService.isTokenValid(token, testUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void isTokenValid_shouldReturnFalseForExpiredToken() throws Exception {
        // Arrange
        // Create a JwtService with a very short expiration time
        JwtService shortExpirationJwtService = new JwtService();
        ReflectionTestUtils.setField(shortExpirationJwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(shortExpirationJwtService, "jwtExpiration", 1); // 1ms expiration

        String token = shortExpirationJwtService.generateToken(testUser);

        // Wait for token to expire
        Thread.sleep(10);

        // Act
        boolean isValid = shortExpirationJwtService.isTokenValid(token, testUser);

        // Assert
        assertFalse(isValid);
    }

    @Test
    void extractUserUUIDFromAuthHeader_shouldReturnCorrectUUID() {
        // Arrange
        String token = jwtService.generateToken(testUser);
        String authHeader = "Bearer " + token;

        // Act
        UUID extractedUuid = jwtService.extractUserUUIDFromAuthHeader(authHeader);

        // Assert
        assertNotNull(extractedUuid);
        assertEquals(testUser.getUuid(), extractedUuid);
    }

    @Test
    void extractUserUUIDFromAuthHeader_shouldReturnNullForInvalidHeader() {
        // Arrange
        String authHeader = "Invalid Header";

        // Act
        UUID extractedUuid = jwtService.extractUserUUIDFromAuthHeader(authHeader);

        // Assert
        assertNull(extractedUuid);
    }
}
