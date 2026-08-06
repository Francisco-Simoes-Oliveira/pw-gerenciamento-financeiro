package com.financeiro.backend.features.profile.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.financeiro.backend.features.profile.dto.request.CreateUserProfileRequest;
import com.financeiro.backend.features.profile.dto.request.UpdateUserProfileRequest;
import com.financeiro.backend.features.profile.dto.response.UserProfileResponse;
import com.financeiro.backend.features.profile.entity.UserProfile;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserProfileMapper {

    @Mapping(target = "user.id", source = "userId")
    UserProfile toEntity(CreateUserProfileRequest request);

    @Mapping(target = "userId", source = "user.id")
    UserProfileResponse toResponse(UserProfile entity);

    void updateEntityFromDto(UpdateUserProfileRequest dto, @MappingTarget UserProfile entity);
}
