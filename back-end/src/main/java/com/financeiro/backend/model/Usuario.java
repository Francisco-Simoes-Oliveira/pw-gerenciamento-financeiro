package com.financeiro.backend.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Entity
@Table(name = "usuarios")
@Data
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotBlank(message = "{name.obrigatorio}")
    private String name;

    @Email(message = "{email.invalido}")
    @NotBlank(message = "{email.obrigatorio}")
    private String email;

    @NotBlank(message = "{password.obrigatorio}")
    private String password;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @NotNull(message = "{active.obrigatorio}")
    private Boolean active;

}
