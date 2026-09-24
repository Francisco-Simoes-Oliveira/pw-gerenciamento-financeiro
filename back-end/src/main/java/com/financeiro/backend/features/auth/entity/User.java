package com.financeiro.backend.features.auth.entity;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import org.hibernate.annotations.UuidGenerator;

import com.financeiro.backend.features.auth.enums.Role;
import com.financeiro.backend.features.profile.entity.UserProfile;

import lombok.Data;

@Entity
@Table(name = "user")
@Data
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @UuidGenerator
    private UUID id;

    @NotBlank(message = "{name.obrigatorio}")
    private String name;

    @Email(message = "{email.invalido}")
    @NotBlank(message = "{email.obrigatorio}")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "{password.obrigatorio}")
    private String password;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NotNull(message = "{active.obrigatorio}")
    private Boolean active;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private UserProfile profile;

    @Override
    public String toString() {
        return "User [id=" + id + ", name=" + name + ", email=" + email + ", active=" + active + ", profile=" + profile
                + "]";
    }
}
