package com.financeiro.backend.security.service.impl;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.security.dto.request.PasswordResetConfirmRequest;
import com.financeiro.backend.security.dto.request.PasswordResetRequest;
import com.financeiro.backend.security.entity.PasswordResetToken;
import com.financeiro.backend.security.repository.PasswordResetTokenRepository;
import com.financeiro.backend.security.service.PasswordResetService;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

    @Autowired
    private PasswordResetTokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void requestPasswordReset(PasswordResetRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário com este email não encontrado."));

        String token = UUID.randomUUID().toString();
        
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .user(user)
                .token(token)
                .expiration(LocalDateTime.now().plusHours(24))
                .used(false)
                .build();

        tokenRepository.save(resetToken);
        
        // Em um sistema real, enviaríamos o email com o link contendo o token.
        System.out.println("Mock Email Sent: Password Reset Token is " + token);
    }

    @Override
    public void confirmPasswordReset(PasswordResetConfirmRequest request) {
        PasswordResetToken resetToken = tokenRepository.findByToken(request.getToken())
                .orElseThrow(() -> new ResourceNotFoundException("Token inválido."));

        if (resetToken.getUsed()) {
            throw new IllegalArgumentException("Este token já foi utilizado.");
        }

        if (resetToken.getExpiration().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Token expirado.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        tokenRepository.save(resetToken);
    }
}
