package com.financeiro.backend.security.services;

import com.financeiro.backend.features.user.entity.User;
import com.financeiro.backend.features.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByEmail(username)
			.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado."));

		List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
			.map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
			.toList();

		return new org.springframework.security.core.userdetails.User(
			user.getEmail(),
			user.getPassword(),
			user.isEnabled(),
			true,
			true,
			true,
			authorities
		);
	}
}
