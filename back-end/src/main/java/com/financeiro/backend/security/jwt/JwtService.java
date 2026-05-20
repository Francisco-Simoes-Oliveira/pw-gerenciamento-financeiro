package com.financeiro.backend.security.jwt;

import com.financeiro.backend.features.user.entity.User;

public interface JwtService {

	String generateToken(User user);

	String extractUsername(String token);

	boolean isTokenValid(String token, String username);
}
