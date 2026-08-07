package com.financeiro.backend.features.category.dto.response;

import java.util.UUID;
import lombok.Data;
import com.financeiro.backend.features.category.enums.CategoryType;

@Data
public class CategoryResponse {
    private UUID id;
    private UUID walletId;
    private String name;
    private String icon;
    private String color;
    private CategoryType type;
    private Boolean active;
    private Boolean systemCategory;
}
