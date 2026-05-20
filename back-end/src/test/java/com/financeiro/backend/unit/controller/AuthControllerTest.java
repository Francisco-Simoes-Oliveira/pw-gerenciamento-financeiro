package com.financeiro.backend.unit.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.financeiro.backend.common.exceptions.GlobalExceptionHandler;
import com.financeiro.backend.features.auth.controller.AuthController;
import com.financeiro.backend.features.auth.dto.AuthResponse;
import com.financeiro.backend.features.auth.dto.LoginRequest;
import com.financeiro.backend.features.auth.service.AuthService;
import com.financeiro.backend.features.user.dto.UserRegisterRequest;
import com.financeiro.backend.features.user.dto.UserResponse;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthControllerTest {

	private final AuthService authService = mock(AuthService.class);
	private MockMvc mockMvc;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService))
			.setControllerAdvice(new GlobalExceptionHandler())
			.build();
	}

	@Test
	void shouldRegisterUser() throws Exception {
		when(authService.register(any(UserRegisterRequest.class))).thenReturn(
			new AuthResponse("jwt-token", "Bearer", 120L, new UserResponse(1L, "Maria Silva", "maria@example.com", Set.of("USER"), true))
		);

		mockMvc.perform(post("/api/v1/auth/register")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(new UserRegisterRequest("Maria Silva", "maria@example.com", "Senha123"))))
			.andExpect(status().isCreated())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.accessToken").value("jwt-token"));
	}

	@Test
	void shouldLoginUser() throws Exception {
		when(authService.login(any(LoginRequest.class))).thenReturn(
			new AuthResponse("jwt-token", "Bearer", 120L, new UserResponse(1L, "Maria Silva", "maria@example.com", Set.of("USER"), true))
		);

		mockMvc.perform(post("/api/v1/auth/login")
			.contentType(MediaType.APPLICATION_JSON)
			.content(objectMapper.writeValueAsString(new LoginRequest("maria@example.com", "Senha123"))))
			.andExpect(status().isOk())
			.andExpect(jsonPath("$.success").value(true))
			.andExpect(jsonPath("$.data.tokenType").value("Bearer"));
	}
}
