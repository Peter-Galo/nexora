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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
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

    @InjectMocks
    private AuthenticationService authenticationService;

    private RegisterRequest registerRequest;
    private AuthenticationRequest authenticationRequest;
    private User testUser;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "John",
                "Doe",
                "john.doe@example.com",
                "password123"
        );

        authenticationRequest = new AuthenticationRequest(
                "john.doe@example.com",
                "password123"
        );

        testUser = new User();
        testUser.setUuid(UUID.randomUUID());
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setPassword("encodedPassword");
        testUser.addRole(Role.USER);
    }

    @Test
    void testRegister_WhenUserDoesNotExist_ShouldCreateUserAndReturnToken() {
        // Given
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUuid(testUser.getUuid()); // Set UUID after "saving"
            return user;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // When
        AuthenticationResponse response = authenticationService.register(registerRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.uuid()).isEqualTo(testUser.getUuid());
        assertThat(response.email()).isEqualTo("john.doe@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.role()).isEqualTo("USER");

        // Verify interactions
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(User.class));
    }

    @Test
    void testRegister_WhenUserAlreadyExists_ShouldThrowApplicationException() {
        // Given
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> authenticationService.register(registerRequest))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("User with email john.doe@example.com already exists");

        // Verify interactions
        verify(userRepository).existsByEmail("john.doe@example.com");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void testRegister_ShouldEncodePasswordAndAddUserRole() {
        // Given
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setUuid(UUID.randomUUID());
            return savedUser;
        });
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // When
        authenticationService.register(registerRequest);

        // Then
        verify(userRepository).save(argThat(user -> {
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(user.getPassword()).isEqualTo("encodedPassword");
            assertThat(user.hasRole(Role.USER)).isTrue();
            return true;
        }));
    }

    @Test
    void testAuthenticate_WhenCredentialsAreValid_ShouldReturnToken() {
        // Given
        when(userRepository.findByEmail(authenticationRequest.email())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);

        // When
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.uuid()).isEqualTo(testUser.getUuid());
        assertThat(response.email()).isEqualTo("john.doe@example.com");
        assertThat(response.firstName()).isEqualTo("John");
        assertThat(response.lastName()).isEqualTo("Doe");
        assertThat(response.role()).isEqualTo("USER");

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(jwtService).generateToken(testUser);
    }

    @Test
    void testAuthenticate_WhenCredentialsAreInvalid_ShouldThrowApplicationException() {
        // Given
        doThrow(new BadCredentialsException("Bad credentials"))
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(authenticationRequest))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Invalid email or password");

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void testAuthenticate_WhenAuthenticationFails_ShouldThrowApplicationException() {
        // Given
        doThrow(new AuthenticationException("Authentication failed") {
        })
                .when(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(authenticationRequest))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("Authentication failed: Authentication failed");

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository, never()).findByEmail(anyString());
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void testAuthenticate_WhenUserNotFoundAfterAuthentication_ShouldThrowApplicationException() {
        // Given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);
        when(userRepository.findByEmail(authenticationRequest.email())).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authenticationService.authenticate(authenticationRequest))
                .isInstanceOf(ApplicationException.class)
                .hasMessage("User not found");

        // Verify interactions
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByEmail("john.doe@example.com");
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @Test
    void testAuthenticate_ShouldPassCorrectCredentialsToAuthenticationManager() {
        // Given
        when(userRepository.findByEmail(authenticationRequest.email())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");

        // When
        authenticationService.authenticate(authenticationRequest);

        // Then
        verify(authenticationManager).authenticate(argThat(token -> {
            assertThat(token).isInstanceOf(UsernamePasswordAuthenticationToken.class);
            assertThat(token.getPrincipal()).isEqualTo("john.doe@example.com");
            assertThat(token.getCredentials()).isEqualTo("password123");
            return true;
        }));
    }


    @Test
    void testCreateAuthenticationResponse_WithMultipleRoles_ShouldJoinRolesWithComma() {
        // Given
        testUser.addRole(Role.ADMIN);
        testUser.addRole(Role.MANAGER);
        when(userRepository.findByEmail(authenticationRequest.email())).thenReturn(Optional.of(testUser));
        when(jwtService.generateToken(testUser)).thenReturn("jwt-token");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(null);

        // When
        AuthenticationResponse response = authenticationService.authenticate(authenticationRequest);

        // Then
        assertThat(response.role()).contains("USER");
        assertThat(response.role()).contains("ADMIN");
        assertThat(response.role()).contains("MANAGER");
        // Roles should be comma-separated
        String[] roles = response.role().split(",");
        assertThat(roles).hasSize(3);
    }

    @Test
    void testRegister_ShouldSetUserFieldsCorrectly() {
        // Given
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.password())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        // Capture the user being saved
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setUuid(UUID.randomUUID());
            return user;
        });

        // When
        authenticationService.register(registerRequest);

        // Then
        verify(userRepository).save(argThat(user -> {
            assertThat(user.getFirstName()).isEqualTo("John");
            assertThat(user.getLastName()).isEqualTo("Doe");
            assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
            assertThat(user.getPassword()).isEqualTo("encodedPassword");
            assertThat(user.getUserRoles()).hasSize(1);
            assertThat(user.hasRole(Role.USER)).isTrue();
            return true;
        }));
    }
}
