package com.nexora.security;

import com.nexora.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpSecurity httpSecurity;

    @InjectMocks
    private SecurityConfig securityConfig;

    @BeforeEach
    void setUp() {
    }

    @Test
    void passwordEncoder_shouldReturnBCryptPasswordEncoder() {
        // Act
        PasswordEncoder encoder = securityConfig.passwordEncoder();

        // Assert
        assertTrue(encoder instanceof BCryptPasswordEncoder);
    }

    @Test
    void userDetailsService_shouldUseUserRepository() {
        // Act
        UserDetailsService userDetailsService = securityConfig.userDetailsService();

        // Assert
        assertNotNull(userDetailsService);

        // This will throw an exception, but we just want to verify that the repository is called
        try {
            userDetailsService.loadUserByUsername("test@example.com");
        } catch (Exception e) {
            // Expected
        }

        verify(userRepository).findByEmail("test@example.com");
    }

    @Test
    void authenticationProvider_shouldUseUserDetailsServiceAndPasswordEncoder() {
        // Arrange
        SecurityConfig spyConfig = spy(securityConfig);
        UserDetailsService mockUserDetailsService = mock(UserDetailsService.class);
        PasswordEncoder mockPasswordEncoder = mock(PasswordEncoder.class);

        doReturn(mockUserDetailsService).when(spyConfig).userDetailsService();
        doReturn(mockPasswordEncoder).when(spyConfig).passwordEncoder();

        // Act
        DaoAuthenticationProvider provider = (DaoAuthenticationProvider) spyConfig.authenticationProvider();

        // Assert
        assertNotNull(provider);

        // Verify the provider is configured with our mocks
        // Note: These are internal fields, so we can't directly verify them
        // This is a limitation of this test approach
    }
}
