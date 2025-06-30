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

    public AuthenticationResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ApplicationException("User with email " + request.getEmail() + " already exists", "USER_ALREADY_EXISTS");
        }

        // Create new user
        var user = new User(
            null, // ID will be generated
            request.getFirstName(),
            request.getLastName(),
            request.getEmail(),
            passwordEncoder.encode(request.getPassword()),
            Role.USER // Default role for new users
        );

        // Save user to database
        userRepository.save(user);

        // Generate JWT token
        var jwtToken = jwtService.generateToken(user);

        // Return authentication response
        return new AuthenticationResponse(
                jwtToken,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }

    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException e) {
            throw new ApplicationException("Invalid email or password", "INVALID_CREDENTIALS");
        } catch (AuthenticationException e) {
            throw new ApplicationException("Authentication failed: " + e.getMessage(), "AUTHENTICATION_FAILED");
        }

        // Find user by email
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApplicationException("User not found", "USER_NOT_FOUND"));

        // Generate JWT token
        var jwtToken = jwtService.generateToken(user);

        // Return authentication response
        return new AuthenticationResponse(
                jwtToken,
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getRole().name()
        );
    }
}
