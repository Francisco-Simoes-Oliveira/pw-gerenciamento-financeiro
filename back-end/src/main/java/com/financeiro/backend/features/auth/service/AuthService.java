package com.financeiro.backend.features.auth.service;

import com.financeiro.backend.features.auth.dto.AuthResponse;
import com.financeiro.backend.features.auth.dto.LoginRequest;
import com.financeiro.backend.features.user.dto.UserRegisterRequest;

public interface AuthService {

	AuthResponse register(UserRegisterRequest request);

	AuthResponse login(LoginRequest request);
}
