package com.financeiro.backend.features.category.dto.request;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.financeiro.backend.features.category.enums.CategoryType;

@Data
public class CreateCategoryRequest {
    private UUID walletId;

    @NotBlank(message = "{name.obrigatorio}")
    private String name;
    
    private String icon;
    private String color;

    @NotNull(message = "{type.obrigatorio}")
    private CategoryType type;

    private Boolean active;
    private Boolean systemCategory;
}
