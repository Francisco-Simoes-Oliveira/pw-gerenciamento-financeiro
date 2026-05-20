package com.financeiro.backend.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.financeiro.backend.common.exceptions.BusinessException;
import com.financeiro.backend.features.user.dto.UserRegisterRequest;
import com.financeiro.backend.features.user.dto.UserResponse;
import com.financeiro.backend.features.user.entity.Role;
import com.financeiro.backend.features.user.entity.User;
import com.financeiro.backend.features.user.enums.RoleName;
import com.financeiro.backend.features.user.mapper.UserMapper;
import com.financeiro.backend.features.user.repository.RoleRepository;
import com.financeiro.backend.features.user.repository.UserRepository;
import com.financeiro.backend.features.user.service.UserServiceImpl;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

	@Mock
	private UserRepository userRepository;

	@Mock
	private RoleRepository roleRepository;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private UserMapper userMapper;

	@InjectMocks
	private UserServiceImpl userService;

	@Test
	void shouldCreateUserWithDefaultRole() {
		UserRegisterRequest request = new UserRegisterRequest("Maria Silva", "maria@example.com", "Senha123");
		Role userRole = Role.builder().id(1L).name(RoleName.USER).build();
		User savedUser = User.builder()
			.id(10L)
			.fullName("Maria Silva")
			.email("maria@example.com")
			.password("encoded-password")
			.enabled(true)
			.roles(Set.of(userRole))
			.build();
		UserResponse expectedResponse = new UserResponse(10L, "Maria Silva", "maria@example.com", Set.of("USER"), true);

		when(userRepository.existsByEmail("maria@example.com")).thenReturn(false);
		when(roleRepository.findByName(RoleName.USER)).thenReturn(Optional.of(userRole));
		when(passwordEncoder.encode("Senha123")).thenReturn("encoded-password");
		when(userRepository.save(any(User.class))).thenReturn(savedUser);
		when(userMapper.toResponse(savedUser)).thenReturn(expectedResponse);

		UserResponse response = userService.create(request);

		assertThat(response).isEqualTo(expectedResponse);
		verify(userRepository).save(any(User.class));
		verify(passwordEncoder).encode("Senha123");
	}

	@Test
	void shouldRejectDuplicateEmail() {
		UserRegisterRequest request = new UserRegisterRequest("Maria Silva", "maria@example.com", "Senha123");
		when(userRepository.existsByEmail("maria@example.com")).thenReturn(true);

		BusinessException exception = assertThrows(BusinessException.class, () -> userService.create(request));

		assertThat(exception.getMessage()).isEqualTo("Já existe um usuário cadastrado com este e-mail.");
	}
}
