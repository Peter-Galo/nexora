package com.nexora.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

/**
 * Response object returned after successful authentication or registration.
 * Immutable record containing user information and JWT token.
 */
@Schema(description = "Response object returned after successful authentication or registration")
public record AuthenticationResponse(
        @Schema(description = "JWT token for authentication", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "User's UUID", example = "5ee0d5d6-5e72-4f73-adfd-691b8c9f136a")
        UUID uuid,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "User's first name", example = "John")
        String firstName,

        @Schema(description = "User's last name", example = "Doe")
        String lastName,

        @Schema(description = "User's role in the system", example = "USER")
        String role
) {
}
