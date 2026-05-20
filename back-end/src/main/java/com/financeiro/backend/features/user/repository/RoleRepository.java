package com.financeiro.backend.features.user.repository;

import com.financeiro.backend.features.user.entity.Role;
import com.financeiro.backend.features.user.enums.RoleName;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long> {

	Optional<Role> findByName(RoleName name);
}
