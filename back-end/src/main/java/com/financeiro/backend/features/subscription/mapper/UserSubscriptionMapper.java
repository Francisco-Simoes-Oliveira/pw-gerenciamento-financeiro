package com.financeiro.backend.features.subscription.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.financeiro.backend.features.subscription.dto.request.CreateUserSubscriptionRequest;
import com.financeiro.backend.features.subscription.dto.request.UpdateUserSubscriptionRequest;
import com.financeiro.backend.features.subscription.dto.response.UserSubscriptionResponse;
import com.financeiro.backend.features.subscription.entity.UserSubscription;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserSubscriptionMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "plan", ignore = true)
    UserSubscription toEntity(CreateUserSubscriptionRequest request);

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "planId", source = "plan.id")
    UserSubscriptionResponse toResponse(UserSubscription entity);

    @Mapping(target = "plan.id", source = "planId")
    void updateEntityFromDto(UpdateUserSubscriptionRequest dto, @MappingTarget UserSubscription entity);
}
