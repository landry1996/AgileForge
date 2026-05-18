package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.UpdateProfileRequest;
import com.agileforge.application.dto.response.ProfileResponse;
import com.agileforge.domain.model.User;
import com.agileforge.domain.port.out.UserRepositoryPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/profile")
@Tag(name = "Profile", description = "User profile management")
public class ProfileController {

    private final UserRepositoryPort userRepository;

    public ProfileController(UserRepositoryPort userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    @Operation(summary = "Get current user profile")
    public ResponseEntity<ProfileResponse> getProfile(Authentication auth) {
        User user = getCurrentUser(auth);
        return ResponseEntity.ok(toResponse(user));
    }

    @PutMapping
    @Operation(summary = "Update current user profile")
    public ResponseEntity<ProfileResponse> updateProfile(Authentication auth,
                                                          @Valid @RequestBody UpdateProfileRequest request) {
        User user = getCurrentUser(auth);

        if (request.firstName() != null) user.setFirstName(request.firstName());
        if (request.lastName() != null) user.setLastName(request.lastName());
        if (request.displayName() != null) user.setDisplayName(request.displayName());
        if (request.avatarUrl() != null) user.setAvatarUrl(request.avatarUrl());
        if (request.phone() != null) user.setPhone(request.phone());
        if (request.timezone() != null) user.setTimezone(request.timezone());
        if (request.locale() != null) user.setLocale(request.locale());

        User updated = userRepository.save(user);
        return ResponseEntity.ok(toResponse(updated));
    }

    private User getCurrentUser(Authentication auth) {
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    private ProfileResponse toResponse(User u) {
        return new ProfileResponse(
                u.getId(), u.getEmail(), u.getFirstName(), u.getLastName(),
                u.getDisplayName(), u.getAvatarUrl(), u.getPhone(),
                u.getTimezone(), u.getLocale(), u.isEmailVerified(),
                u.getLastLoginAt(), u.getCreatedAt()
        );
    }
}
