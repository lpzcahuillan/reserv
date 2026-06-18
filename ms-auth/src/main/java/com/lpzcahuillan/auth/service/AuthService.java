package com.lpzcahuillan.auth.service;

import com.lpzcahuillan.auth.dto.AuthRequest;
import com.lpzcahuillan.auth.dto.AuthResponse;
import com.lpzcahuillan.auth.dto.RegisterRequest;
import com.lpzcahuillan.auth.entity.User;

public interface AuthService {
    User register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
}
