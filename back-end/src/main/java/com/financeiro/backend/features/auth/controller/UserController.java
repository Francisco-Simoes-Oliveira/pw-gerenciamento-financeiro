package com.financeiro.backend.features.auth.controller;

import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.common.dto.ApiResponse;
import com.financeiro.backend.features.auth.dto.request.CreateUserRequest;
import com.financeiro.backend.features.auth.dto.request.UpdateUserRequest;
import com.financeiro.backend.features.auth.dto.response.UserResponse;
import com.financeiro.backend.features.auth.service.UserService;

@RestController
@RequestMapping("/api/users")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(service.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> searchById(@PathVariable UUID id) {
        UserResponse user = service.searchById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> insert(@Valid @RequestBody CreateUserRequest request) {
        UserResponse savedUser = service.insert(request);
        return ResponseEntity.ok(ApiResponse.success(savedUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> alter(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        UserResponse updatedUser = service.alter(id, request);
        return ResponseEntity.ok(ApiResponse.success(updatedUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> remove(@PathVariable UUID id) {
        service.remove(id);
        return ResponseEntity.ok(ApiResponse.success(null, "User successfully deleted."));
    }
}
