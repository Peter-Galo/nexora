package com.nexora.service;

import com.nexora.dto.AuthenticationRequest;
import com.nexora.dto.AuthenticationResponse;
import com.nexora.dto.RegisterRequest;
import com.nexora.exception.ApplicationException;
import com.nexora.model.Role;
import com.nexora.model.User;
import com.nexora.repository.UserRepository;
import com.nexora.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        authenticationService = new AuthenticationService(
                userRepository,
                passwordEncoder,
                jwtService,
                authenticationManager
        );
    }

    @Test
    void register_shouldCreateNewUserAndReturnToken() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setFirstName("John");
        registerRequest.setLastName("Doe");
        registerRequest.setEmail("john.doe@example.com");
        registerRequest.setPassword("password123");

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setUuid(UUID.randomUUID());
        savedUser.setFirstName(registerRequest.getFirstName());
        savedUser.setLastName(registerRequest.getLastName());
        savedUser.setEmail(registerRequest.getEmail());
        savedUser.setPassword("encodedPassword");
        savedUser.addRole(Role.USER);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwtToken");

        // Act
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals(savedUser.getEmail(), response.getEmail());
        assertEquals(savedUser.getFirstName(), response.getFirstName());
        assertEquals(savedUser.getLastName(), response.getLastName());
        assertEquals(Role.USER.name(), response.getRole());

        // Verify user was saved with correct data
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User capturedUser = userCaptor.getValue();

        assertEquals(registerRequest.getFirstName(), capturedUser.getFirstName());
        assertEquals(registerRequest.getLastName(), capturedUser.getLastName());
        assertEquals(registerRequest.getEmail(), capturedUser.getEmail());
        assertEquals("encodedPassword", capturedUser.getPassword());
        assertTrue(capturedUser.hasRole(Role.USER));
    }

    @Test
    void register_withExistingEmail_shouldThrowException() {
        // Arrange
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setEmail("existing@example.com");

        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> authenticationService.register(registerRequest)
        );

        assertEquals("User with email existing@example.com already exists", exception.getMessage());
        assertEquals("USER_ALREADY_EXISTS", exception.getCode());

        // Verify user was not saved
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticate_withValidCredentials_shouldReturnToken() {
        // Arrange
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setEmail("john.doe@example.com");
        authRequest.setPassword("password123");

        User user = new User();
        user.setUuid(UUID.randomUUID());
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail(authRequest.getEmail());
        user.setPassword("encodedPassword");
        user.addRole(Role.USER);

        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwtToken");

        // Act
        AuthenticationResponse response = authenticationService.authenticate(authRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getFirstName(), response.getFirstName());
        assertEquals(user.getLastName(), response.getLastName());
        assertEquals(Role.USER.name(), response.getRole());

        // Verify authentication was attempted
        verify(authenticationManager).authenticate(
                new UsernamePasswordAuthenticationToken(
                        authRequest.getEmail(),
                        authRequest.getPassword()
                )
        );
    }

    @Test
    void authenticate_withInvalidCredentials_shouldThrowException() {
        // Arrange
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setEmail("john.doe@example.com");
        authRequest.setPassword("wrongPassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> authenticationService.authenticate(authRequest)
        );

        assertEquals("Invalid email or password", exception.getMessage());
        assertEquals("INVALID_CREDENTIALS", exception.getCode());
    }

    @Test
    void authenticate_withNonExistentUser_shouldThrowException() {
        // Arrange
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setEmail("nonexistent@example.com");
        authRequest.setPassword("password123");

        // Authentication passes but user not found in repository
        when(userRepository.findByEmail(authRequest.getEmail())).thenReturn(Optional.empty());

        // Act & Assert
        ApplicationException exception = assertThrows(
                ApplicationException.class,
                () -> authenticationService.authenticate(authRequest)
        );

        assertEquals("User not found", exception.getMessage());
        assertEquals("USER_NOT_FOUND", exception.getCode());
    }
}
