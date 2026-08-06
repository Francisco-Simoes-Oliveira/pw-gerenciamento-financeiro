package com.financeiro.backend.features.category.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.financeiro.backend.features.category.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, UUID> {
}
