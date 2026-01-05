package com.jendo.app.domain.user.service;

import com.jendo.app.common.dto.PaginationResponse;
import com.jendo.app.domain.user.dto.UserRequestDto;
import com.jendo.app.domain.user.dto.UserResponseDto;
import com.jendo.app.domain.user.dto.UserUpdateDto;
import com.jendo.app.domain.user.dto.UserNameEmailDto;
import java.util.List;

public interface UserService {
    
    UserResponseDto createUser(UserRequestDto request);
    
    UserResponseDto getUserById(Long id);
    
    UserResponseDto getUserByEmail(String email);
    
    PaginationResponse<UserResponseDto> getAllUsers(int page, int size);
    
    PaginationResponse<UserResponseDto> searchUsers(String query, int page, int size);
    
    UserResponseDto updateUser(Long id, UserUpdateDto request);
    
    void deleteUser(Long id);
    
    List<UserNameEmailDto> getAllUsersNamesAndEmails();
}
