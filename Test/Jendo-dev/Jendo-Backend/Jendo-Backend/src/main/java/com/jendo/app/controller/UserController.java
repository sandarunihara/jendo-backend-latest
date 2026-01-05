package com.jendo.app.controller;

import com.jendo.app.common.dto.ApiResponse;
import com.jendo.app.common.dto.PaginationResponse;
import com.jendo.app.domain.user.dto.UserRequestDto;
import com.jendo.app.domain.user.dto.UserResponseDto;
import com.jendo.app.domain.user.dto.UserUpdateDto;
import com.jendo.app.domain.user.dto.UserNameEmailDto;
import com.jendo.app.domain.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import java.io.IOException;
import java.util.Map;
import java.util.List;
import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "User management APIs")

public class UserController {
    private final UserService userService;
    private final Cloudinary cloudinary;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with the provided details")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already exists")
    })
    public ResponseEntity<ApiResponse<UserResponseDto>> createUser(@Valid @RequestBody UserRequestDto request) {
        UserResponseDto user = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(user, "User created successfully"));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their unique identifier")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserById(
            @Parameter(description = "User ID") @PathVariable Long id) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieves a user by their email address")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserByEmail(
            @Parameter(description = "User email") @PathVariable String email) {
        UserResponseDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Retrieves the currently authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        UserResponseDto user = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PutMapping("/me")
    @Operation(summary = "Update current user profile", description = "Updates the currently authenticated user's profile")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateCurrentUser(
            Authentication authentication,
            @Valid @RequestBody UserUpdateDto request) {
        String email = authentication.getName();
        UserResponseDto currentUser = userService.getUserByEmail(email);
        UserResponseDto user = userService.updateUser(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(user, "Profile updated successfully"));
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all users with pagination")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponseDto>>> getAllUsers(
            @Parameter(description = "Page number (0-indexed)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "10") int size) {
        PaginationResponse<UserResponseDto> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @GetMapping("/search")
    @Operation(summary = "Search users", description = "Search users by name")
    public ResponseEntity<ApiResponse<PaginationResponse<UserResponseDto>>> searchUsers(
            @Parameter(description = "Search query") @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        PaginationResponse<UserResponseDto> users = userService.searchUsers(query, page, size);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto request) {
        UserResponseDto user = userService.updateUser(id, request);
        return ResponseEntity.ok(ApiResponse.success(user, "User updated successfully"));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Deletes a user by ID")
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "User deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/all/names-emails")
    @Operation(summary = "Get all users names and emails", description = "Retrieves all users with their names and emails only")
    public ResponseEntity<ApiResponse<List<UserNameEmailDto>>> getAllUsersNamesAndEmails() {
        List<UserNameEmailDto> users = userService.getAllUsersNamesAndEmails();
        return ResponseEntity.ok(ApiResponse.success(users, "Users retrieved successfully"));
    }

    @PostMapping(value = "/profile-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload profile image", description = "Uploads a profile image for the current user")
    public ResponseEntity<ApiResponse<UserResponseDto>> uploadProfileImage(
            Authentication authentication,
            @RequestParam("image") MultipartFile file) {
        try {
            // Validate file
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Please select a file to upload"));
            }

            // Check file type
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("Only image files are allowed"));
            }

            // Check file size (max 5MB)
            if (file.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body(ApiResponse.error("File size must not exceed 5MB"));
            }

            // Upload to Cloudinary
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.emptyMap());
            String imageUrl = (String) uploadResult.get("secure_url");

            // Update user profile with new image URL
            String email = authentication.getName();
            UserResponseDto currentUser = userService.getUserByEmail(email);
            UserUpdateDto updateDto = UserUpdateDto.builder()
                    .profileImage(imageUrl)
                    .build();
            UserResponseDto updatedUser = userService.updateUser(currentUser.getId(), updateDto);

            // Return updated user object with success message
            return ResponseEntity.ok(ApiResponse.success(updatedUser, "Profile image uploaded successfully"));
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload file: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("Failed to upload to Cloudinary: " + e.getMessage()));
        }
    }
}
