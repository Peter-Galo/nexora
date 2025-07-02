package com.nexora.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexora.dto.AuthenticationRequest;
import com.nexora.dto.AuthenticationResponse;
import com.nexora.dto.RegisterRequest;
import com.nexora.exception.ApplicationException;
import com.nexora.exception.GlobalExceptionHandler;
import com.nexora.model.Role;
import com.nexora.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private ObjectMapper objectMapper;
    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private AuthenticationResponse authenticationResponse;
    private UUID testUuid;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization

        // Create test data
        testUuid = UUID.randomUUID();

        // Create test register request
        registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("StrongP@ss123");

        // Create test authentication request
        authenticationRequest = new AuthenticationRequest();
        authenticationRequest.setEmail("john.doe@example.com");
        authenticationRequest.setPassword("StrongP@ss123");

        // Create test authentication response
        authenticationResponse = new AuthenticationResponse(
                "jwtToken",
                testUuid,
                "john.doe@example.com",
                "John",
                "Doe",
                Role.USER.name()
        );
    }

    @Test
    void register_withValidData_shouldRegisterUserAndReturnToken() throws Exception {
        // Arrange
        when(authenticationService.register(any(RegisterRequest.class))).thenReturn(authenticationResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is("jwtToken")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.role", is("USER")));

        verify(authenticationService).register(any(RegisterRequest.class));
    }

    @Test
    void register_withExistingEmail_shouldReturnBadRequest() throws Exception {
        // Arrange
        when(authenticationService.register(any(RegisterRequest.class)))
                .thenThrow(new ApplicationException("User with email john.doe@example.com already exists", "USER_ALREADY_EXISTS"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("USER_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.messages[0]", containsString("already exists")));

        verify(authenticationService).register(any(RegisterRequest.class));
    }

    @Test
    void register_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Arrange
        RegisterRequest invalidRequest = new RegisterRequest();
        invalidRequest.setFirstName(""); // Invalid: empty first name
        invalidRequest.setLastName("Doe");
        invalidRequest.setEmail("invalid-email"); // Invalid: not an email format
        invalidRequest.setPassword("weak"); // Invalid: doesn't meet password requirements

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.messages", hasSize(greaterThan(0))));

        verify(authenticationService, never()).register(any(RegisterRequest.class));
    }

    @Test
    void authenticate_withValidCredentials_shouldReturnToken() throws Exception {
        // Arrange
        when(authenticationService.authenticate(any(AuthenticationRequest.class))).thenReturn(authenticationResponse);

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token", is("jwtToken")))
                .andExpect(jsonPath("$.email", is("john.doe@example.com")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")))
                .andExpect(jsonPath("$.role", is("USER")));

        verify(authenticationService).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    void authenticate_withInvalidCredentials_shouldReturnBadRequest() throws Exception {
        // Arrange
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new ApplicationException("Invalid email or password", "INVALID_CREDENTIALS"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("INVALID_CREDENTIALS")))
                .andExpect(jsonPath("$.messages[0]", containsString("Invalid email or password")));

        verify(authenticationService).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    void authenticate_withNonExistentUser_shouldReturnBadRequest() throws Exception {
        // Arrange
        when(authenticationService.authenticate(any(AuthenticationRequest.class)))
                .thenThrow(new ApplicationException("User not found", "USER_NOT_FOUND"));

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authenticationRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("USER_NOT_FOUND")))
                .andExpect(jsonPath("$.messages[0]", containsString("User not found")));

        verify(authenticationService).authenticate(any(AuthenticationRequest.class));
    }

    @Test
    void authenticate_withInvalidData_shouldReturnBadRequest() throws Exception {
        // Arrange
        AuthenticationRequest invalidRequest = new AuthenticationRequest();
        invalidRequest.setEmail("invalid-email"); // Invalid: not an email format
        invalidRequest.setPassword(""); // Invalid: empty password

        // Act & Assert
        mockMvc.perform(post("/api/v1/auth/authenticate")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")))
                .andExpect(jsonPath("$.messages", hasSize(greaterThan(0))));

        verify(authenticationService, never()).authenticate(any(AuthenticationRequest.class));
    }
}