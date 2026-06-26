package com.lpzcahuillan.auth.service;

import com.lpzcahuillan.auth.dto.AuthRequest;
import com.lpzcahuillan.auth.dto.AuthResponse;
import com.lpzcahuillan.auth.dto.RegisterRequest;
import com.lpzcahuillan.auth.entity.User;
import com.lpzcahuillan.auth.exception.BadRequestException;
import com.lpzcahuillan.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    void register_Success() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setPassword("password123");
        request.setEmail("new@example.com");
        
        Set<String> roles = new HashSet<>();
        roles.add("USER");
        request.setRoles(roles);

        User savedUser = User.builder()
                .id(1L)
                .username("newUser")
                .password("encodedPassword")
                .email("new@example.com")
                .roles(Set.of("ROLE_USER"))
                .build();

        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // When
        User result = authService.register(request);

        // Then
        assertNotNull(result);
        assertEquals("newUser", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_UsernameAlreadyTaken() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existingUser");

        when(userRepository.existsByUsername("existingUser")).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertEquals("Username is already taken", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_EmailAlreadyRegistered() {
        // Given
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newUser");
        request.setEmail("existing@example.com");

        when(userRepository.existsByUsername("newUser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.register(request));
        assertEquals("Email is already registered", exception.getMessage());
    }

    @Test
    void login_Success() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPassword("password");

        User user = User.builder()
                .username("user")
                .password("encodedPassword")
                .roles(Set.of("ROLE_USER"))
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtService.generateToken("user", Set.of("ROLE_USER"))).thenReturn("jwtToken");

        // When
        AuthResponse response = authService.login(request);

        // Then
        assertNotNull(response);
        assertEquals("jwtToken", response.getToken());
        assertEquals("user", response.getUsername());
    }

    @Test
    void login_InvalidUsername() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setUsername("nonexistent");

        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(request));
        assertEquals("Invalid username or password", exception.getMessage());
    }

    @Test
    void login_InvalidPassword() {
        // Given
        AuthRequest request = new AuthRequest();
        request.setUsername("user");
        request.setPassword("wrongPassword");

        User user = User.builder()
                .username("user")
                .password("encodedPassword")
                .build();

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        // When & Then
        BadRequestException exception = assertThrows(BadRequestException.class, () -> authService.login(request));
        assertEquals("Invalid username or password", exception.getMessage());
    }
}
