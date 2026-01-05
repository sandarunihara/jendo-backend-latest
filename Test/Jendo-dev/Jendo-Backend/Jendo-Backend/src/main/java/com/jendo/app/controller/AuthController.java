package com.jendo.app.controller;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import com.jendo.app.common.dto.ApiResponse;
import com.jendo.app.domain.user.dto.*;
import com.jendo.app.domain.user.entity.OtpToken;
import com.jendo.app.domain.user.entity.LabOtpToken;
import com.jendo.app.domain.user.entity.User;
import com.jendo.app.domain.user.mapper.UserMapper;
import com.jendo.app.domain.user.repository.OtpTokenRepository;
import com.jendo.app.domain.user.repository.LabOtpTokenRepository;
import com.jendo.app.domain.user.repository.UserRepository;
import com.jendo.app.domain.user.dto.LabOtpRequestDto;
import com.jendo.app.domain.user.dto.LabOtpValidationDto;
import com.jendo.app.domain.user.service.EmailService;
import com.jendo.app.domain.user.service.UserService;
import com.jendo.app.security.JwtUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/auth")

@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Authentication and authorization APIs")
public class AuthController {
    private final EmailService emailService;
    private final OtpTokenRepository otpRepo;
    private final LabOtpTokenRepository labOtpTokenRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    
    private static final SecureRandom secureRandom = new SecureRandom();
    
    @Value("${google.client-id:}")
    private String googleClientId;

    private String generateSecureOtp() {
        int otp = 100000 + secureRandom.nextInt(900000);
        return String.valueOf(otp);
    }

    @PostMapping("/send-otp")
    @Transactional
    @Operation(summary = "Send OTP to email", description = "Sends a one-time password to the specified email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        
        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            log.warn("Attempt to send OTP to existing email: {}", email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("An account with this email already exists"));
        }
        
        String otp = generateSecureOtp();
        String hashedOtp = passwordEncoder.encode(otp);
        
        OtpToken token = new OtpToken();
        token.setEmail(email);
        token.setOtp(hashedOtp);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepo.deleteByEmail(email);
        otpRepo.save(token);
        
        emailService.sendOtpEmail(email, otp);
        log.info("OTP sent to email: {}", email);
        
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("success", true, "message", "OTP sent to your email"),
            "OTP sent successfully"
        ));
    }

    @PostMapping("/verify-otp")
    @Transactional
    @Operation(summary = "Verify OTP", description = "Verifies the OTP sent to email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> verifyOtp(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String otp = req.get("otp");
        
        Optional<OtpToken> tokenOpt = otpRepo.findByEmail(email);
        if (tokenOpt.isPresent() && tokenOpt.get().getExpiresAt().isAfter(LocalDateTime.now())) {
            if (passwordEncoder.matches(otp, tokenOpt.get().getOtp())) {
                otpRepo.deleteByEmail(email);
                log.info("OTP verified for email: {}", email);
                return ResponseEntity.ok(ApiResponse.success(
                    Map.of("success", true, "verified", true),
                    "OTP verified successfully"
                ));
            }
        }
        
        log.warn("Invalid or expired OTP for email: {}", email);
        return ResponseEntity.status(400).body(ApiResponse.error("Invalid or expired OTP"));
    }

    @PostMapping("/login")
    @Transactional(readOnly = true)
    @Operation(summary = "User login", description = "Authenticates user and returns JWT tokens")
    public ResponseEntity<ApiResponse<AuthResponseDto>> login(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        String password = req.get("password");
        
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
            
            User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
            
            String token = jwtUtil.generateToken(email, user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(email, user.getId());
            UserResponseDto userDto = userMapper.toResponseDto(user);
            boolean profileComplete = isProfileComplete(user);
            
            AuthResponseDto response = AuthResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .user(userDto)
                .profileComplete(profileComplete)
                .build();
            
            log.info("User logged in successfully: {}", email);
            return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
            
        } catch (Exception e) {
            log.error("Login failed for email: {}", email, e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid email or password"));
        }
    }

    @PostMapping("/signup")
    @Operation(summary = "User registration", description = "Creates a new user account")
    public ResponseEntity<ApiResponse<AuthResponseDto>> signup(@Valid @RequestBody UserRequestDto req) {
        try {
            UserResponseDto createdUser = userService.createUser(req);
            
            String token = jwtUtil.generateToken(createdUser.getEmail(), createdUser.getId());
            String refreshToken = jwtUtil.generateRefreshToken(createdUser.getEmail(), createdUser.getId());
            
            User user = userRepository.findById(createdUser.getId()).orElseThrow();
            boolean profileComplete = isProfileComplete(user);
            
            AuthResponseDto response = AuthResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(createdUser.getId())
                .email(createdUser.getEmail())
                .fullName(createdUser.getFirstName() + " " + createdUser.getLastName())
                .user(createdUser)
                .profileComplete(profileComplete)
                .build();
            
            log.info("User registered successfully: {}", createdUser.getEmail());
            return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Registration successful"));
                
        } catch (Exception e) {
            log.error("Registration failed", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Registration failed: " + e.getMessage()));
        }
    }

    @PostMapping("/forgot-password")
    @Transactional
    @Operation(summary = "Request password reset", description = "Sends OTP to email for password reset")
    public ResponseEntity<ApiResponse<Map<String, Object>>> forgotPassword(@RequestBody Map<String, String> req) {
        String email = req.get("email");
        
        if (!userRepository.existsByEmail(email)) {
            return ResponseEntity.ok(ApiResponse.success(
                Map.of("success", true, "message", "If an account exists, an OTP will be sent"),
                "Password reset initiated"
            ));
        }
        
        String otp = generateSecureOtp();
        String hashedOtp = passwordEncoder.encode(otp);
        
        OtpToken token = new OtpToken();
        token.setEmail(email);
        token.setOtp(hashedOtp);
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        otpRepo.deleteByEmail(email);
        otpRepo.save(token);
        
        emailService.sendPasswordResetOtp(email, otp);
        log.info("Password reset OTP sent to: {}", email);
        
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("success", true, "message", "If an account exists, an OTP will be sent"),
            "Password reset initiated"
        ));
    }

    @PostMapping("/reset-password")
    @Transactional
    @Operation(summary = "Reset password with OTP", description = "Resets password after OTP verification")
    public ResponseEntity<ApiResponse<Map<String, Object>>> resetPassword(@Valid @RequestBody ResetPasswordDto req) {
        String email = req.getEmail();
        String otp = req.getOtp();
        String newPassword = req.getNewPassword();
        
        Optional<OtpToken> tokenOpt = otpRepo.findByEmail(email);
        if (tokenOpt.isEmpty() || tokenOpt.get().getExpiresAt().isBefore(LocalDateTime.now())) {
            log.warn("Invalid or expired reset OTP for: {}", email);
            return ResponseEntity.status(400).body(ApiResponse.error("Invalid or expired OTP"));
        }
        
        if (!passwordEncoder.matches(otp, tokenOpt.get().getOtp())) {
            log.warn("OTP mismatch for: {}", email);
            return ResponseEntity.status(400).body(ApiResponse.error("Invalid OTP"));
        }
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("User not found"));
        }
        
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        otpRepo.deleteByEmail(email);
        log.info("Password reset successfully for: {}", email);
        
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("success", true),
            "Password reset successfully"
        ));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change password", description = "Changes password for authenticated user")
    public ResponseEntity<ApiResponse<Map<String, Object>>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordDto req) {
        
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required"));
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            log.warn("Invalid current password for: {}", email);
            return ResponseEntity.status(400).body(ApiResponse.error("Current password is incorrect"));
        }
        
        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed successfully for: {}", email);
        
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("success", true),
            "Password changed successfully"
        ));
    }

    @PostMapping("/google")
    @Transactional
    @Operation(summary = "Google OAuth login", description = "Authenticates user with Google OAuth")
    public ResponseEntity<ApiResponse<AuthResponseDto>> googleAuth(@Valid @RequestBody GoogleAuthDto req) {
        try {
            Map<String, Object> googleUserInfo = verifyGoogleToken(req.getIdToken());
            
            if (googleUserInfo == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid Google token"));
            }
            
            String email = (String) googleUserInfo.get("email");
            String googleId = (String) googleUserInfo.get("sub");
            String firstName = (String) googleUserInfo.getOrDefault("given_name", "");
            String lastName = (String) googleUserInfo.getOrDefault("family_name", "");
            String picture = (String) googleUserInfo.getOrDefault("picture", "");
            
            Object emailVerifiedObj = googleUserInfo.getOrDefault("email_verified", false);
            boolean emailVerified = false;
            if (emailVerifiedObj instanceof Boolean) {
                emailVerified = (Boolean) emailVerifiedObj;
            } else if (emailVerifiedObj instanceof String) {
                emailVerified = "true".equalsIgnoreCase((String) emailVerifiedObj);
            }
            if (!emailVerified) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Google email not verified"));
            }
            
            User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.findByGoogleId(googleId)
                    .orElse(null));
            
            if (user == null) {
                user = User.builder()
                    .email(email)
                    .firstName(firstName)
                    .lastName(lastName)
                    .googleId(googleId)
                    .authProvider("google")
                    .profileImage(picture)
                    .emailVerified(true)
                    .password(passwordEncoder.encode(java.util.UUID.randomUUID().toString()))
                    .build();
                user = userRepository.save(user);
                log.info("New Google user created: {}", email);
            } else {
                if (user.getGoogleId() == null) {
                    user.setGoogleId(googleId);
                    user.setAuthProvider("google");
                    user.setEmailVerified(true);
                    userRepository.save(user);
                }
            }
            
            String token = jwtUtil.generateToken(email, user.getId());
            String refreshToken = jwtUtil.generateRefreshToken(email, user.getId());
            UserResponseDto userDto = userMapper.toResponseDto(user);
            boolean profileComplete = isProfileComplete(user);
            
            AuthResponseDto response = AuthResponseDto.builder()
                .token(token)
                .refreshToken(refreshToken)
                .userId(user.getId())
                .email(user.getEmail())
                .fullName(user.getFirstName() + " " + user.getLastName())
                .user(userDto)
                .profileComplete(profileComplete)
                .build();
            
            log.info("Google authentication successful for: {}", email);
            return ResponseEntity.ok(ApiResponse.success(response, "Google login successful"));
            
        } catch (Exception e) {
            log.error("Google authentication failed", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Google authentication failed: " + e.getMessage()));
        }
    }

    @PostMapping("/refresh")
    @Transactional(readOnly = true)
    @Operation(summary = "Refresh token", description = "Refreshes access token using refresh token")
    public ResponseEntity<ApiResponse<AuthResponseDto>> refreshToken(@RequestBody Map<String, String> req) {
        String refreshToken = req.get("refreshToken");
        
        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Refresh token is required"));
        }
        
        if (!jwtUtil.validateJwtToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid refresh token"));
        }
        
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            log.warn("Attempted to refresh using non-refresh token");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Invalid token type - must use refresh token"));
        }
        
        if (jwtUtil.isTokenExpired(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Refresh token has expired"));
        }
        
        String email = jwtUtil.getEmailFromToken(refreshToken);
        Long userId = jwtUtil.getUserIdFromToken(refreshToken);
        
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.getId().equals(userId)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("User not found or token mismatch"));
        }
        
        String newAccessToken = jwtUtil.generateToken(email, userId);
        String newRefreshToken = jwtUtil.generateRefreshToken(email, userId);
        UserResponseDto userDto = userMapper.toResponseDto(user);
        
        AuthResponseDto response = AuthResponseDto.builder()
            .token(newAccessToken)
            .refreshToken(newRefreshToken)
            .userId(userId)
            .email(email)
            .fullName(user.getFirstName() + " " + user.getLastName())
            .user(userDto)
            .profileComplete(isProfileComplete(user))
            .build();
        
        log.info("Token refreshed for user: {}", email);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed"));
    }

    @GetMapping("/me")
    @Transactional(readOnly = true)
    @Operation(summary = "Get current user", description = "Returns authenticated user profile with completion status")
    public ResponseEntity<ApiResponse<AuthResponseDto>> getCurrentUser(Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Authentication required"));
        }
        
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        UserResponseDto userDto = userMapper.toResponseDto(user);
        boolean profileComplete = isProfileComplete(user);
        
        AuthResponseDto response = AuthResponseDto.builder()
            .userId(user.getId())
            .email(user.getEmail())
            .fullName(user.getFirstName() + " " + user.getLastName())
            .user(userDto)
            .profileComplete(profileComplete)
            .build();
        
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout", description = "Invalidates user session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> logout() {
        return ResponseEntity.ok(ApiResponse.success(
            Map.of("success", true),
            "Logged out successfully"
        ));
    }

    private boolean isProfileComplete(User user) {
        return user.getFirstName() != null && !user.getFirstName().isEmpty()
            && user.getLastName() != null && !user.getLastName().isEmpty()
            && user.getEmail() != null && !user.getEmail().isEmpty()
            && user.getPhone() != null && !user.getPhone().isEmpty()
            && user.getDateOfBirth() != null
            && user.getGender() != null && !user.getGender().isEmpty();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> verifyGoogleToken(String idToken) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            
            Map<String, Object> tokenInfo = restTemplate.getForObject(tokenInfoUrl, Map.class);
            
            if (tokenInfo == null) {
                log.error("Failed to verify Google token - null response");
                return null;
            }
            
            String audience = (String) tokenInfo.get("aud");
            if (googleClientId != null && !googleClientId.isEmpty() && !googleClientId.equals(audience)) {
                log.error("Google token audience mismatch: expected {}, got {}", googleClientId, audience);
                return null;
            }
            
            String issuer = (String) tokenInfo.get("iss");
            if (!"accounts.google.com".equals(issuer) && !"https://accounts.google.com".equals(issuer)) {
                log.error("Invalid Google token issuer: {}", issuer);
                return null;
            }
            
            String expStr = (String) tokenInfo.get("exp");
            if (expStr != null) {
                long exp = Long.parseLong(expStr);
                if (exp < System.currentTimeMillis() / 1000) {
                    log.error("Google token has expired");
                    return null;
                }
            }
            
            return tokenInfo;
        } catch (Exception e) {
            log.error("Failed to verify Google token", e);
            return null;
        }
    }

    /**
     * Endpoint to send OTP to lab assistant's email
     * POST /api/auth/lab-send-otp
     * Request body: { "email": "user@example.com" }
     * Response: OTP sent to email (displayed in logs for development)
     */
    @PostMapping("/lab-send-otp")
    @Transactional
    @Operation(summary = "Send Lab OTP to email", description = "Generates and sends a 6-digit OTP to the specified email")
    public ResponseEntity<ApiResponse<Map<String, Object>>> sendLabOtp(@Valid @RequestBody LabOtpRequestDto request) {
        String email = request.getEmail();
        
        log.info("Processing lab OTP request for email: {}", email);
        
        // Verify user exists
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.warn("User not found for email: {}", email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("User with this email does not exist"));
        }
        
        // Generate 6-digit OTP
        String otp = generateSecureOtp();
        log.info("=== LAB OTP for {} : {} ===", email, otp);
        
        // Delete any existing OTP for this email
        labOtpTokenRepository.deleteByUserEmail(email);
        
        // Save OTP token with 1 hour expiration and null labAssistantId
        LabOtpToken labOtpToken = LabOtpToken.builder()
            .otp(otp)
            .userEmail(email)
            .expireTime(LocalDateTime.now().plusHours(1))
            .labAssistantId(null)
            .build();
        
        labOtpTokenRepository.save(labOtpToken);
        log.info("Lab OTP saved for email: {}", email);
        
        // Send OTP via email
        emailService.sendOtpEmail(email, otp);
        
        Map<String, Object> response = Map.of(
            "message", "OTP has been sent to your email",
            "email", email,
            "expiresIn", "60 minutes"
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "OTP sent successfully"));
    }

    /**
     * Endpoint to validate OTP and return user details
     * GET /api/auth/lab-validate-otp
     * Request params: email, otp, labAssistantId
     * Response: User full details if OTP is valid
     */
    @GetMapping("/lab-validate-otp")
    @Transactional
    @Operation(summary = "Validate Lab OTP", description = "Validates the OTP for lab assistant and returns user details if valid")
    public ResponseEntity<ApiResponse<?>> validateLabOtp(
            @RequestParam String email,
            @RequestParam String otp,
            @RequestParam Long labAssistantId) {
        
        log.info("Processing lab OTP validation for email: {} with labAssistantId: {}", email, labAssistantId);
        
        // Delete expired tokens
        labOtpTokenRepository.deleteExpiredTokens(LocalDateTime.now());
        
        // Find valid OTP token
        Optional<LabOtpToken> otpTokenOptional = 
            labOtpTokenRepository.findByUserEmailAndOtpAndExpireTimeAfter(email, otp, LocalDateTime.now());
        
        if (otpTokenOptional.isEmpty()) {
            log.warn("Invalid or expired OTP for email: {}", email);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error("Invalid or expired OTP"));
        }
        
        // Get user details
        Optional<User> userOptional = userRepository.findByEmail(email);
        if (userOptional.isEmpty()) {
            log.warn("User not found for email: {}", email);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error("User not found"));
        }
        
        User user = userOptional.get();
        LabOtpToken otpToken = otpTokenOptional.get();
        
        // Update labAssistantId in the OTP token
        otpToken.setLabAssistantId(labAssistantId);
        labOtpTokenRepository.save(otpToken);
        log.info("Lab assistant ID updated for email: {}", email);
        
        // Delete this OTP token after successful validation
//        labOtpTokenRepository.delete(otpToken);
//        log.info("Lab OTP token deleted for email: {}", email);
        
        // Return user details
        Object userDetails = userMapper.toResponseDto(user);
        
        Map<String, Object> response = Map.of(
            "message", "OTP validated successfully",
            "user", userDetails,
            "labAssistantId", labAssistantId
        );
        
        return ResponseEntity.ok(ApiResponse.success(response, "OTP validated successfully"));
    }
}
