

package com.financeiro.backend.model;

import java.util.UUID;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Entity
@Table(name = "perfis")
@Data
class Perfil {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
   
    @NotBlank(message = "{descricao.obrigatorio}")
    private String descricao;
}
    