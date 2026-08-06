package com.financeiro.backend.features.auth.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

import com.financeiro.backend.common.exception.ResourceNotFoundException;
import com.financeiro.backend.features.auth.dto.request.CreateUserRequest;
import com.financeiro.backend.features.auth.dto.response.UserResponse;
import com.financeiro.backend.features.auth.entity.User;
import com.financeiro.backend.features.auth.mapper.UserMapper;
import com.financeiro.backend.features.auth.repository.UserRepository;
import com.financeiro.backend.features.auth.service.impl.UserServiceImpl;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void testSearchById_Success() {
        UUID id = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(id);
        mockUser.setName("Test");

        UserResponse mockResponse = new UserResponse();
        mockResponse.setId(id);
        mockResponse.setName("Test");

        when(userRepository.findById(id)).thenReturn(Optional.of(mockUser));
        when(userMapper.toResponse(mockUser)).thenReturn(mockResponse);

        UserResponse response = userService.searchById(id);

        assertNotNull(response);
        assertEquals("Test", response.getName());
        verify(userRepository).findById(id);
    }

    @Test
    void testSearchById_NotFound() {
        UUID id = UUID.randomUUID();
        
        when(userRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.searchById(id));
    }

    @Test
    void testInsert_Success() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Test");
        
        User user = new User();
        user.setName("Test");
        
        User savedUser = new User();
        savedUser.setId(UUID.randomUUID());
        savedUser.setName("Test");
        
        UserResponse mockResponse = new UserResponse();
        mockResponse.setId(savedUser.getId());
        mockResponse.setName("Test");

        when(userMapper.toEntity(request)).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(userMapper.toResponse(savedUser)).thenReturn(mockResponse);

        UserResponse response = userService.insert(request);

        assertNotNull(response);
        assertEquals("Test", response.getName());
        verify(userRepository).save(any(User.class));
    }
}
