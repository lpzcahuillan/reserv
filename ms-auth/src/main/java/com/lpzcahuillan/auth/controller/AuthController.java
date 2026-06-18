package com.lpzcahuillan.auth.controller;

import com.lpzcahuillan.auth.dto.AuthRequest;
import com.lpzcahuillan.auth.dto.AuthResponse;
import com.lpzcahuillan.auth.dto.RegisterRequest;
import com.lpzcahuillan.auth.entity.User;
import com.lpzcahuillan.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
