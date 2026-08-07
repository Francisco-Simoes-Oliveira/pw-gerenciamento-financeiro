package com.financeiro.backend.features.category.dto.request;

import lombok.Data;
import com.financeiro.backend.features.category.enums.CategoryType;

@Data
public class UpdateCategoryRequest {
    private String name;
    private String icon;
    private String color;
    private CategoryType type;
    private Boolean active;
}
