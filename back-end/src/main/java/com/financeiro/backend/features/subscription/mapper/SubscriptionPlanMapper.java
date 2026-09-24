package com.financeiro.backend.features.subscription.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.ReportingPolicy;

import com.financeiro.backend.features.subscription.dto.request.CreateSubscriptionPlanRequest;
import com.financeiro.backend.features.subscription.dto.request.UpdateSubscriptionPlanRequest;
import com.financeiro.backend.features.subscription.dto.response.SubscriptionPlanResponse;
import com.financeiro.backend.features.subscription.entity.SubscriptionPlan;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SubscriptionPlanMapper {
    SubscriptionPlan toEntity(CreateSubscriptionPlanRequest request);
    SubscriptionPlanResponse toResponse(SubscriptionPlan entity);
    void updateEntityFromDto(UpdateSubscriptionPlanRequest dto, @MappingTarget SubscriptionPlan entity);
}
