package com.financeiro.backend.features.auth.service;

import com.financeiro.backend.common.exceptions.ResourceNotFoundException;
import com.financeiro.backend.features.auth.dto.AuthResponse;
import com.financeiro.backend.features.auth.dto.LoginRequest;
import com.financeiro.backend.features.user.dto.UserRegisterRequest;
import com.financeiro.backend.features.user.dto.UserResponse;
import com.financeiro.backend.features.user.entity.User;
import com.financeiro.backend.features.user.mapper.UserMapper;
import com.financeiro.backend.features.user.repository.UserRepository;
import com.financeiro.backend.features.user.service.UserService;
import com.financeiro.backend.security.jwt.JwtProperties;
import com.financeiro.backend.security.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthServiceImpl implements AuthService {

	private final UserService userService;
	private final UserRepository userRepository;
	private final JwtService jwtService;
	private final AuthenticationManager authenticationManager;
	private final JwtProperties jwtProperties;
	private final UserMapper userMapper;

	@Override
	public AuthResponse register(UserRegisterRequest request) {
		UserResponse createdUser = userService.create(request);
		User savedUser = userRepository.findByEmail(request.email().trim().toLowerCase())
			.orElseThrow(() -> new ResourceNotFoundException("Usuário recém-cadastrado não encontrado."));

		return new AuthResponse(
			jwtService.generateToken(savedUser),
			"Bearer",
			jwtProperties.expirationMinutes(),
			createdUser
		);
	}

	@Override
	public AuthResponse login(LoginRequest request) {
		authenticationManager.authenticate(
			new UsernamePasswordAuthenticationToken(request.email().trim().toLowerCase(), request.password())
		);

		User user = userRepository.findByEmail(request.email().trim().toLowerCase())
			.orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

		return new AuthResponse(
			jwtService.generateToken(user),
			"Bearer",
			jwtProperties.expirationMinutes(),
			userMapper.toResponse(user)
		);
	}
}
