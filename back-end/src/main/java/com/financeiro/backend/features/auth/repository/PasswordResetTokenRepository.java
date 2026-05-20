package com.financeiro.backend.features.auth.repository;

import com.financeiro.backend.features.auth.entity.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);
}
