package com.financeiro.backend.features.user.service;

import com.financeiro.backend.common.exceptions.BusinessException;
import com.financeiro.backend.common.exceptions.ResourceNotFoundException;
import com.financeiro.backend.features.user.dto.UserRegisterRequest;
import com.financeiro.backend.features.user.dto.UserResponse;
import com.financeiro.backend.features.user.entity.Role;
import com.financeiro.backend.features.user.entity.User;
import com.financeiro.backend.features.user.enums.RoleName;
import com.financeiro.backend.features.user.mapper.UserMapper;
import com.financeiro.backend.features.user.repository.RoleRepository;
import com.financeiro.backend.features.user.repository.UserRepository;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;
	private final UserMapper userMapper;

	@Override
	public UserResponse create(UserRegisterRequest request) {
		if (userRepository.existsByEmail(request.email().trim().toLowerCase(Locale.ROOT))) {
			throw new BusinessException("Já existe um usuário cadastrado com este e-mail.");
		}

		Role userRole = roleRepository.findByName(RoleName.USER)
			.orElseGet(() -> roleRepository.save(Role.builder().name(RoleName.USER).build()));

		User user = User.builder()
			.fullName(request.fullName().trim())
			.email(request.email().trim().toLowerCase(Locale.ROOT))
			.password(passwordEncoder.encode(request.password()))
			.enabled(true)
			.build();
		user.addRole(userRole);

		return userMapper.toResponse(userRepository.save(user));
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse findById(Long id) {
		User user = userRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
		return userMapper.toResponse(user);
	}

	@Override
	@Transactional(readOnly = true)
	public UserResponse findByEmail(String email) {
		User user = userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
			.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
		return userMapper.toResponse(user);
	}
}
