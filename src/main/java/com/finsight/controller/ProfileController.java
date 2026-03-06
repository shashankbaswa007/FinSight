package com.finsight.controller;

import com.finsight.dto.ChangePasswordRequest;
import com.finsight.dto.ProfileResponse;
import com.finsight.dto.UpdateProfileRequest;
import com.finsight.exception.BadRequestException;
import com.finsight.exception.ResourceNotFoundException;
import com.finsight.model.User;
import com.finsight.repository.UserRepository;
import com.finsight.util.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Profile", description = "Manage user profile and settings")
public class ProfileController {

    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, SecurityUtil securityUtil, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.securityUtil = securityUtil;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    @Operation(summary = "Get profile", description = "Get the current user's profile")
    public ResponseEntity<ProfileResponse> getProfile() {
        User user = getCurrentUser();
        return ResponseEntity.ok(mapToResponse(user));
    }

    @PutMapping
    @Operation(summary = "Update profile", description = "Update name and/or email")
    public ResponseEntity<ProfileResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        User user = getCurrentUser();

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new BadRequestException("Email is already in use");
            }
            user.setEmail(request.getEmail());
        }

        user = userRepository.save(user);
        return ResponseEntity.ok(mapToResponse(user));
    }

    @PutMapping("/password")
    @Operation(summary = "Change password", description = "Change the current user's password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        User user = getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser() {
        Long userId = securityUtil.getCurrentUserId();
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    private ProfileResponse mapToResponse(User user) {
        return ProfileResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole().name())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
