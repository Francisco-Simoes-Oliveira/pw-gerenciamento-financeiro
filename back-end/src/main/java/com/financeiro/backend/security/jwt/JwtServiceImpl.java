package com.financeiro.backend.security.jwt;

import com.financeiro.backend.features.user.entity.Role;
import com.financeiro.backend.features.user.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceImpl implements JwtService {

	private final SecretKey jwtSecretKey;
	private final JwtProperties jwtProperties;

	@Override
	public String generateToken(User user) {
		Instant now = Instant.now();
		Instant expiresAt = now.plus(Duration.ofMinutes(jwtProperties.expirationMinutes()));
		List<String> roles = user.getRoles().stream().map(Role::getName).map(Enum::name).toList();

		return Jwts.builder()
			.subject(user.getEmail())
			.issuer(jwtProperties.issuer())
			.issuedAt(Date.from(now))
			.expiration(Date.from(expiresAt))
			.claim("userId", user.getId())
			.claim("roles", roles)
			.signWith(jwtSecretKey, Jwts.SIG.HS256)
			.compact();
	}

	@Override
	public String extractUsername(String token) {
		return parseClaims(token).getSubject();
	}

	@Override
	public boolean isTokenValid(String token, String username) {
		try {
			Claims claims = parseClaims(token);
			return username.equalsIgnoreCase(claims.getSubject()) && claims.getExpiration().after(Date.from(Instant.now()));
		} catch (JwtException exception) {
			return false;
		}
	}

	private Claims parseClaims(String token) {
		return Jwts.parser()
			.verifyWith(jwtSecretKey)
			.build()
			.parseSignedClaims(token)
			.getPayload();
	}
}
