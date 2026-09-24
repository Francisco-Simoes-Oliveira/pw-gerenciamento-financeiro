package com.financeiro.backend.features.category.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.financeiro.backend.features.category.dto.request.CreateCategoryRequest;
import com.financeiro.backend.features.category.dto.request.UpdateCategoryRequest;
import com.financeiro.backend.features.category.dto.response.CategoryResponse;
import com.financeiro.backend.features.category.entity.Category;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CategoryMapper {
    @Mapping(target = "wallet", ignore = true)
    Category toEntity(CreateCategoryRequest request);

    @Mapping(target = "walletId", source = "wallet.id")
    CategoryResponse toResponse(Category entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "wallet", ignore = true)
    @Mapping(target = "systemCategory", ignore = true)
    void updateEntityFromDto(UpdateCategoryRequest dto, @MappingTarget Category entity);
}
