package com.jendo.app.domain.user.service;

import com.jendo.app.common.dto.PaginationResponse;
import com.jendo.app.common.exceptions.ConflictException;
import com.jendo.app.common.exceptions.NotFoundException;
import com.jendo.app.domain.user.dto.UserRequestDto;
import com.jendo.app.domain.user.dto.UserResponseDto;
import com.jendo.app.domain.user.dto.UserUpdateDto;
import com.jendo.app.domain.user.dto.UserNameEmailDto;
import com.jendo.app.domain.user.entity.Role;
import com.jendo.app.domain.user.entity.User;
import com.jendo.app.domain.user.mapper.UserMapper;
import com.jendo.app.domain.user.repository.RoleRepository;
import com.jendo.app.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto createUser(UserRequestDto request) {
        logger.info("Creating new user with email: {}", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            logger.warn("User creation failed - email already exists: {}", request.getEmail());
            throw new ConflictException("User with email " + request.getEmail() + " already exists");
        }

        User user = userMapper.toEntity(request);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user = userRepository.save(user);

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            User finalUser = user;
            List<Role> roles = request.getRoles().stream()
                    .map(roleName -> Role.builder()
                            .roleName(roleName)
                            .user(finalUser)
                            .build())
                    .collect(Collectors.toList());
            roleRepository.saveAll(roles);
            user.setRoles(roles);
        }

        logger.info("User created successfully with ID: {}", user.getId());
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long id) {
        logger.info("Fetching user with ID: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found with ID: {}", id);
                    return new NotFoundException("User", id);
                });
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserByEmail(String email) {
        logger.info("Fetching user with email: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.warn("User not found with email: {}", email);
                    return new NotFoundException("User not found with email: " + email);
                });
        return userMapper.toResponseDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserResponseDto> getAllUsers(int page, int size) {
        logger.info("Fetching all users - page: {}, size: {}", page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findAll(pageable);

        return buildPaginationResponse(userPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PaginationResponse<UserResponseDto> searchUsers(String query, int page, int size) {
        logger.info("Searching users with query: {} - page: {}, size: {}", query, page, size);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<User> userPage = userRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
                query, query, pageable);

        return buildPaginationResponse(userPage);
    }

    @Override
    public UserResponseDto updateUser(Long id, UserUpdateDto request) {
        logger.info("Updating user with ID: {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("User not found for update with ID: {}", id);
                    return new NotFoundException("User", id);
                });

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                logger.warn("User update failed - email already exists: {}", request.getEmail());
                throw new ConflictException("User with email " + request.getEmail() + " already exists");
            }
            user.setEmail(request.getEmail());
        }

        if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
        if (request.getLastName() != null) user.setLastName(request.getLastName());
        if (request.isPhoneSet()) user.setPhone(request.getPhone());
        if (request.isDateOfBirthSet()) user.setDateOfBirth(request.getDateOfBirth());
        if (request.isGenderSet()) user.setGender(request.getGender());
        if (request.getProfileImage() != null) user.setProfileImage(request.getProfileImage());
        if (request.isNationalitySet()) user.setNationality(request.getNationality());
        if (request.isAddressSet()) user.setAddress(request.getAddress());
        if (request.isWeightSet()) user.setWeight(request.getWeight());
        if (request.isHeightSet()) user.setHeight(request.getHeight());

        if (request.getRoles() != null) {
            roleRepository.deleteByUserId(id);
            final User userForRoles = user;
            List<Role> roles = request.getRoles().stream()
                    .map(roleName -> Role.builder()
                            .roleName(roleName)
                            .user(userForRoles)
                            .build())
                    .collect(Collectors.toList());
            roleRepository.saveAll(roles);
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        logger.info("User updated successfully with ID: {}", id);
        return userMapper.toResponseDto(savedUser);
    }

    @Override
    public void deleteUser(Long id) {
        logger.info("Deleting user with ID: {}", id);

        if (!userRepository.existsById(id)) {
            logger.warn("User not found for deletion with ID: {}", id);
            throw new NotFoundException("User", id);
        }

        userRepository.deleteById(id);
        logger.info("User deleted successfully with ID: {}", id);
    }

    private PaginationResponse<UserResponseDto> buildPaginationResponse(Page<User> userPage) {
        List<UserResponseDto> content = userPage.getContent().stream()
                .map(userMapper::toResponseDto)
                .collect(Collectors.toList());

        return PaginationResponse.<UserResponseDto>builder()
                .content(content)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .first(userPage.isFirst())
                .last(userPage.isLast())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserNameEmailDto> getAllUsersNamesAndEmails() {
        logger.info("Fetching all users names and emails");
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> UserNameEmailDto.builder()
                        .id(user.getId())
                        .fullName(user.getFirstName() + " " + user.getLastName())
                        .email(user.getEmail())
                        .build())
                .collect(Collectors.toList());
    }
}
