package com.financeiro.backend.features.auth.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.financeiro.backend.features.auth.dto.request.CreateUserRequest;
import com.financeiro.backend.features.auth.dto.request.UpdateUserRequest;
import com.financeiro.backend.features.auth.dto.response.UserResponse;
import com.financeiro.backend.features.auth.entity.User;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {
    User toEntity(CreateUserRequest request);
    UserResponse toResponse(User entity);
    void updateEntityFromDto(UpdateUserRequest dto, @MappingTarget User entity);
}
