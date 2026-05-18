package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.EntityNotFoundException;
import com.agileforge.domain.exception.UnauthorizedException;
import com.agileforge.domain.model.RefreshToken;
import com.agileforge.domain.model.User;
import com.agileforge.domain.port.in.AuthenticationUseCase;
import com.agileforge.domain.port.in.RegisterUseCase;
import com.agileforge.domain.port.out.PasswordEncoderPort;
import com.agileforge.domain.port.out.RefreshTokenRepositoryPort;
import com.agileforge.domain.port.out.UserRepositoryPort;
import com.agileforge.infrastructure.security.JwtService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class AuthService implements RegisterUseCase, AuthenticationUseCase {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepositoryPort userRepository;
    private final RefreshTokenRepositoryPort refreshTokenRepository;
    private final PasswordEncoderPort passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepositoryPort userRepository,
                       RefreshTokenRepositoryPort refreshTokenRepository,
                       PasswordEncoderPort passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Override
    public User register(String email, String password, String firstName, String lastName) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("Email already registered");
        }

        String hashedPassword = passwordEncoder.encode(password);
        User user = new User(email, hashedPassword, firstName, lastName);
        User saved = userRepository.save(user);

        log.info("User registered: {}", email);
        return saved;
    }

    @Override
    public AuthResult login(String email, String password, String ipAddress, String userAgent) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        if (user.isLocked()) {
            throw new UnauthorizedException("Account is temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.recordFailedLogin();
            userRepository.save(user);
            throw new UnauthorizedException("Invalid credentials");
        }

        user.recordSuccessfulLogin();
        userRepository.save(user);

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), Map.of());
        String refreshTokenValue = jwtService.generateRefreshTokenValue();

        RefreshToken refreshToken = new RefreshToken(
                user.getId(),
                refreshTokenValue,
                Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()),
                ipAddress,
                userAgent
        );
        refreshTokenRepository.save(refreshToken);

        log.info("User logged in: {}", email);
        return new AuthResult(user, accessToken, refreshTokenValue);
    }

    @Override
    public AuthResult refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(refreshTokenValue)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

        if (!refreshToken.isValid()) {
            throw new UnauthorizedException("Refresh token expired or revoked");
        }

        User user = userRepository.findById(refreshToken.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("User", refreshToken.getUserId()));

        if (!user.isActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        // Revoke old token and issue new pair
        refreshToken.revoke();
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getEmail(), Map.of());
        String newRefreshTokenValue = jwtService.generateRefreshTokenValue();

        RefreshToken newRefreshToken = new RefreshToken(
                user.getId(),
                newRefreshTokenValue,
                Instant.now().plusMillis(jwtService.getRefreshTokenExpiration()),
                null,
                null
        );
        refreshTokenRepository.save(newRefreshToken);

        return new AuthResult(user, newAccessToken, newRefreshTokenValue);
    }

    @Override
    public void logout(String refreshTokenValue) {
        refreshTokenRepository.findByToken(refreshTokenValue)
                .ifPresent(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
    }

    @Override
    public void logoutAll(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
        log.info("All sessions revoked for user: {}", userId);
    }
}
