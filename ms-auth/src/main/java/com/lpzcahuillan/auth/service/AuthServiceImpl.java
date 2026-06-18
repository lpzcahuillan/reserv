package com.lpzcahuillan.auth.service;

import com.lpzcahuillan.auth.dto.AuthRequest;
import com.lpzcahuillan.auth.dto.AuthResponse;
import com.lpzcahuillan.auth.dto.RegisterRequest;
import com.lpzcahuillan.auth.entity.User;
import com.lpzcahuillan.auth.exception.BadRequestException;
import com.lpzcahuillan.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User register(RegisterRequest request) {
        log.info("Registrando nuevo usuario: {}", request.getUsername());
        
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered");
        }

        Set<String> roles = request.getRoles();
        if (roles == null || roles.isEmpty()) {
            roles = new HashSet<>();
            roles.add("ROLE_CUSTOMER"); // Default role
        } else {
            Set<String> formattedRoles = new HashSet<>();
            for (String role : roles) {
                if (!role.startsWith("ROLE_")) {
                    formattedRoles.add("ROLE_" + role.toUpperCase());
                } else {
                    formattedRoles.add(role.toUpperCase());
                }
            }
            roles = formattedRoles;
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .roles(roles)
                .build();

        User saved = userRepository.save(user);
        log.info("Usuario registrado exitosamente: {}", saved.getUsername());
        return saved;
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        log.info("Intento de login para usuario: {}", request.getUsername());
        
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username or password");
        }

        String token = jwtService.generateToken(user.getUsername(), user.getRoles());
        log.info("Login exitoso para usuario: {}", user.getUsername());
        
        return new AuthResponse(token, user.getUsername(), user.getRoles());
    }
}
