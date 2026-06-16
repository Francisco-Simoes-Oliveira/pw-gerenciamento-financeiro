package com.financeiro.backend.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.financeiro.backend.model.Profile;
import com.financeiro.backend.service.ProfileService;

@RestController
@RequestMapping("/api/profiles")
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<List<Profile>> listAll() {
        return ResponseEntity.ok(profileService.listAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Profile> searchById(@PathVariable UUID id) {
        Profile profile = profileService.searchById(id);
        return ResponseEntity.ok(profile);
    }

    @PostMapping
    public ResponseEntity<Profile> insert(@RequestBody Profile profile) {
        Profile savedProfile = profileService.insert(profile);
        return ResponseEntity.ok(savedProfile);
    }

    @PutMapping
    public ResponseEntity<Profile> alter(@RequestBody Profile profile) {
        Profile updatedProfile = profileService.alter(profile);
        return ResponseEntity.ok(updatedProfile);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {
        profileService.remove(id);
        return ResponseEntity.noContent().build();
    }
    
    
}
