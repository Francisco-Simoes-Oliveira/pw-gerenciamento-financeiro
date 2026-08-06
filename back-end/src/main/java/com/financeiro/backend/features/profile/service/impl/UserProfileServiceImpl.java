package com.financeiro.backend.features.profile.service.impl;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.profile.dto.request.CreateUserProfileRequest;
import com.financeiro.backend.features.profile.dto.request.UpdateUserProfileRequest;
import com.financeiro.backend.features.profile.dto.response.UserProfileResponse;
import com.financeiro.backend.features.profile.entity.UserProfile;
import com.financeiro.backend.features.profile.mapper.UserProfileMapper;
import com.financeiro.backend.features.profile.repository.UserProfileRepository;
import com.financeiro.backend.features.profile.service.UserProfileService;

@Service
public class UserProfileServiceImpl implements UserProfileService {

    @Autowired
    private UserProfileRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileMapper mapper;

    @Override
    public UserProfileResponse getByUserId(UUID userId) {
        UserProfile profile = repository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado para o usuário: " + userId));
        return mapper.toResponse(profile);
    }

    @Override
    public UserProfileResponse insert(CreateUserProfileRequest request) {
        if (repository.findByUserId(request.getUserId()).isPresent()) {
            throw new IllegalArgumentException("O usuário já possui um perfil.");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + request.getUserId()));

        UserProfile profile = mapper.toEntity(request);
        profile.setUser(user);

        UserProfile saved = repository.save(profile);
        return mapper.toResponse(saved);
    }

    @Override
    public UserProfileResponse alter(UUID userId, UpdateUserProfileRequest request) {
        UserProfile profile = repository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil não encontrado para o usuário: " + userId));

        mapper.updateEntityFromDto(request, profile);
        UserProfile updated = repository.save(profile);
        return mapper.toResponse(updated);
    }
}
