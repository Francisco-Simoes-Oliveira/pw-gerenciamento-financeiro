package com.financeiro.backend.features.profile.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.profile.dto.request.CreateUserProfileRequest;
import com.financeiro.backend.features.profile.dto.response.UserProfileResponse;
import com.financeiro.backend.features.profile.entity.UserProfile;
import com.financeiro.backend.features.profile.mapper.UserProfileMapper;
import com.financeiro.backend.features.profile.repository.UserProfileRepository;
import com.financeiro.backend.features.profile.service.impl.UserProfileServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileMapper profileMapper;

    @InjectMocks
    private UserProfileServiceImpl profileService;

    @Test
    void testInsert_Success() {
        UUID userId = UUID.randomUUID();
        CreateUserProfileRequest request = new CreateUserProfileRequest();
        request.setUserId(userId);
        request.setFullName("Test Profile");

        User user = new User();
        user.setId(userId);

        UserProfile profile = new UserProfile();
        profile.setFullName("Test Profile");

        UserProfile savedProfile = new UserProfile();
        savedProfile.setId(UUID.randomUUID());
        savedProfile.setFullName("Test Profile");
        savedProfile.setUser(user);

        UserProfileResponse responseMock = new UserProfileResponse();
        responseMock.setUserId(userId);
        responseMock.setFullName("Test Profile");

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(profileMapper.toEntity(request)).thenReturn(profile);
        when(profileRepository.save(any(UserProfile.class))).thenReturn(savedProfile);
        when(profileMapper.toResponse(savedProfile)).thenReturn(responseMock);

        UserProfileResponse response = profileService.insert(request);

        assertNotNull(response);
        assertEquals("Test Profile", response.getFullName());
        assertEquals(userId, response.getUserId());
        verify(profileRepository).save(any(UserProfile.class));
    }
}
