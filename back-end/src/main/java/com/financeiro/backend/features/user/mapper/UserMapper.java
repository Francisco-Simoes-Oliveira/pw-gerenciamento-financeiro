package com.financeiro.backend.features.user.mapper;

import com.financeiro.backend.features.user.dto.UserResponse;
import com.financeiro.backend.features.user.entity.Role;
import com.financeiro.backend.features.user.entity.User;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

	public UserResponse toResponse(User user) {
		Set<String> roles = user.getRoles().stream()
			.map(Role::getName)
			.map(Enum::name)
			.collect(Collectors.toUnmodifiableSet());

		return new UserResponse(user.getId(), user.getFullName(), user.getEmail(), roles, user.isEnabled());
	}
}
