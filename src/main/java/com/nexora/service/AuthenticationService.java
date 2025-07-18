package com.nexora.service;

import com.nexora.dto.AuthenticationRequest;
import com.nexora.dto.AuthenticationResponse;
import com.nexora.dto.RegisterRequest;
import com.nexora.exception.ApplicationException;
import com.nexora.model.Role;
import com.nexora.model.User;
import com.nexora.repository.UserRepository;
import com.nexora.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuthenticationManager authenticationManager
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }

    @Transactional
    public AuthenticationResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.email())) {
            throw new ApplicationException("User with email " + request.email() + " already exists", "USER_ALREADY_EXISTS");
        }

        // Create new user
        var user = new User(
                null, // UUID will be generated
                request.firstName(),
                request.lastName(),
                request.email(),
                passwordEncoder.encode(request.password())
        );

        // Add default USER role
        user.addRole(Role.USER);

        // Save user to database
        userRepository.save(user);

        // Generate JWT token and return response
        return createAuthenticationResponse(user);
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(),
                            request.password()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ApplicationException("Invalid email or password", "INVALID_CREDENTIALS");
        } catch (AuthenticationException e) {
            throw new ApplicationException("Authentication failed: " + e.getMessage(), "AUTHENTICATION_FAILED");
        }

        // Find user by email
        var user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ApplicationException("User not found", "USER_NOT_FOUND"));

        // Generate JWT token and return response
        return createAuthenticationResponse(user);
    }

    /**
     * Creates an AuthenticationResponse from a User entity.
     * Centralizes the logic for creating authentication responses.
     *
     * @param user the User entity
     * @return the AuthenticationResponse
     */
    private AuthenticationResponse createAuthenticationResponse(User user) {
        var jwtToken = jwtService.generateToken(user);
        var roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));

        return new AuthenticationResponse(
                jwtToken,
                user.getUuid(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                roles
        );
    }
}
