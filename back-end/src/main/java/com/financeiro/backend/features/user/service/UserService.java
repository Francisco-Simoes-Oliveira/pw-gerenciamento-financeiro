package com.financeiro.backend.features.user.service;

import com.financeiro.backend.features.user.dto.UserRegisterRequest;
import com.financeiro.backend.features.user.dto.UserResponse;

public interface UserService {

	UserResponse create(UserRegisterRequest request);

	UserResponse findById(Long id);

	UserResponse findByEmail(String email);
}
