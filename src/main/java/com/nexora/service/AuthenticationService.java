package com.nexora.service;

import com.nexora.dto.AuthenticationRequest;
import com.nexora.dto.AuthenticationResponse;
import com.nexora.dto.RegisterRequest;
import com.nexora.model.Role;
import com.nexora.model.User;
import com.nexora.repository.UserRepository;
import com.nexora.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
            throw new RuntimeException("User with email " + request.getEmail() + " already exists");
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
        // Authenticate user
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // Find user by email
        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

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
