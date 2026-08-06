

package com.financeiro.backend.features.profile.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import com.financeiro.backend.features.auth.entity.User;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "UserProfile")
@Data
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String fullName;
    
    private java.time.LocalDate birthDate;
    
    private String phone;

    private String avatarUrl;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    
}
    