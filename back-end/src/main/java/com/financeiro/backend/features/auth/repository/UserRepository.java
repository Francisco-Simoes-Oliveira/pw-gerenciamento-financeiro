package com.financeiro.backend.features.auth.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.enums.Role;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    java.util.Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    long countByRole(Role role);
}
