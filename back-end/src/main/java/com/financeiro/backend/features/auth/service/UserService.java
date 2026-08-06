package com.financeiro.backend.features.auth.service;

import java.util.List;
import java.util.UUID;

import com.financeiro.backend.features.auth.dto.request.CreateUserRequest;
import com.financeiro.backend.features.auth.dto.request.UpdateUserRequest;
import com.financeiro.backend.features.auth.dto.response.UserResponse;

public interface UserService {
    UserResponse insert(CreateUserRequest request);
    List<UserResponse> listAll();
    UserResponse searchById(UUID id);
    void remove(UUID id);
    UserResponse alter(UUID id, UpdateUserRequest request);
}
