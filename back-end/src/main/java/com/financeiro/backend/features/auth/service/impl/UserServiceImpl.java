package com.financeiro.backend.features.auth.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.dto.request.CreateUserRequest;
import com.financeiro.backend.features.auth.dto.request.UpdateUserRequest;
import com.financeiro.backend.features.auth.dto.response.UserResponse;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.mapper.UserMapper;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.auth.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository repository;

    @Autowired
    private UserMapper mapper;

    @Override
    public UserResponse insert(CreateUserRequest request) {
        User user = mapper.toEntity(request);
        user.setActive(true);
        user.setCreatedAt(LocalDateTime.now());
        User saved = repository.save(user);
        return mapper.toResponse(saved);
    }

    @Override
    public List<UserResponse> listAll() {
        return repository.findAll().stream()
                .map(mapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse searchById(UUID id) {
        User user = findEntityById(id);
        return mapper.toResponse(user);
    }

    @Override
    public void remove(UUID id) {
        User user = findEntityById(id);
        repository.delete(user);
    }

    @Override
    public UserResponse alter(UUID id, UpdateUserRequest request) {
        User userDB = findEntityById(id);
        mapper.updateEntityFromDto(request, userDB);
        userDB.setUpdatedAt(LocalDateTime.now());
        User updated = repository.save(userDB);
        return mapper.toResponse(updated);
    }

    private User findEntityById(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com id " + id));
    }
}
