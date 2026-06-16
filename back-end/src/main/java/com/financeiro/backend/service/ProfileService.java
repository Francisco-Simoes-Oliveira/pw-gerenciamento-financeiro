package com.financeiro.backend.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.financeiro.backend.model.Profile;
import com.financeiro.backend.repository.ProfileRepository;

@Service
public class ProfileService {
    @Autowired
    private ProfileRepository repository;

    public List<Profile> listAll() {
        return repository.findAll();
    }

    public Profile searchById(UUID id) {
        Profile profile = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Profile não encontrado com o ID: " + id));
        return profile;
    }

    public Profile insert(Profile profile) {
        return repository.save(profile);
    }

    public Profile alter(Profile profile) {
        if (profile.getId() == null) {
            throw new IllegalArgumentException("ID do perfil é obrigatório para alteração");
        }

        searchById(profile.getId());
        return repository.save(profile);
    }

    public void remove(UUID id) {
        Profile profile = searchById(id);
        repository.delete(profile);
    }
}
