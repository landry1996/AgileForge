package com.agileforge.infrastructure.web.controller;

import com.agileforge.application.dto.request.LoginRequest;
import com.agileforge.application.dto.request.RefreshTokenRequest;
import com.agileforge.application.dto.request.RegisterRequest;
import com.agileforge.application.dto.response.AuthResponse;
import com.agileforge.application.dto.response.UserResponse;
import com.agileforge.application.mapper.UserMapper;
import com.agileforge.domain.model.User;
import com.agileforge.domain.port.in.AuthenticationUseCase;
import com.agileforge.domain.port.in.RegisterUseCase;
import com.agileforge.domain.port.out.UserRepositoryPort;
import com.agileforge.infrastructure.security.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {

    private final RegisterUseCase registerUseCase;
    private final AuthenticationUseCase authenticationUseCase;
    private final UserRepositoryPort userRepository;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public AuthController(RegisterUseCase registerUseCase,
                          AuthenticationUseCase authenticationUseCase,
                          UserRepositoryPort userRepository,
                          UserMapper userMapper,
                          JwtService jwtService) {
        this.registerUseCase = registerUseCase;
        this.authenticationUseCase = authenticationUseCase;
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        User user = registerUseCase.register(
                request.email(),
                request.password(),
                request.firstName(),
                request.lastName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(userMapper.toResponse(user));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        String ipAddress = extractIpAddress(httpRequest);
        String userAgent = httpRequest.getHeader("User-Agent");

        AuthenticationUseCase.AuthResult result = authenticationUseCase.login(
                request.email(), request.password(), ipAddress, userAgent);

        AuthResponse response = new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                jwtService.getAccessTokenExpiration() / 1000,
                userMapper.toResponse(result.user())
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationUseCase.AuthResult result = authenticationUseCase.refreshToken(request.refreshToken());

        AuthResponse response = new AuthResponse(
                result.accessToken(),
                result.refreshToken(),
                jwtService.getAccessTokenExpiration() / 1000,
                userMapper.toResponse(result.user())
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout (revoke refresh token)")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authenticationUseCase.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/logout-all")
    @Operation(summary = "Logout from all devices")
    public ResponseEntity<Void> logoutAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        userRepository.findByEmail(email).ifPresent(user ->
                authenticationUseCase.logoutAll(user.getId()));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @Operation(summary = "Get current authenticated user")
    public ResponseEntity<UserResponse> me() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .map(userMapper::toResponse)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private String extractIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
