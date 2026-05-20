package com.financeiro.backend.features.user.controller;

import com.financeiro.backend.common.responses.ApiResponse;
import com.financeiro.backend.features.user.dto.UserResponse;
import com.financeiro.backend.features.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping("/{id}")
	public ResponseEntity<ApiResponse<UserResponse>> findById(@PathVariable Long id) {
		return ResponseEntity.ok(ApiResponse.success("Usuário encontrado com sucesso.", userService.findById(id)));
	}

	@GetMapping("/by-email")
	public ResponseEntity<ApiResponse<UserResponse>> findByEmail(@RequestParam String email) {
		return ResponseEntity.ok(ApiResponse.success("Usuário encontrado com sucesso.", userService.findByEmail(email)));
	}
}
