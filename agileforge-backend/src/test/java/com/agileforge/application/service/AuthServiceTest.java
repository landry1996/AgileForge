package com.agileforge.application.service;

import com.agileforge.domain.exception.BusinessException;
import com.agileforge.domain.exception.UnauthorizedException;
import com.agileforge.domain.model.RefreshToken;
import com.agileforge.domain.model.User;
import com.agileforge.domain.port.in.AuthenticationUseCase;
import com.agileforge.domain.port.out.PasswordEncoderPort;
import com.agileforge.domain.port.out.RefreshTokenRepositoryPort;
import com.agileforge.domain.port.out.UserRepositoryPort;
import com.agileforge.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests")
class AuthServiceTest {

    @Mock
    private UserRepositoryPort userRepository;

    @Mock
    private RefreshTokenRepositoryPort refreshTokenRepository;

    @Mock
    private PasswordEncoderPort passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Nested
    @DisplayName("Register")
    class RegisterTests {

        @Test
        @DisplayName("Should register a new user successfully")
        void shouldRegisterNewUser() {
            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(UUID.randomUUID());
                return user;
            });

            User result = authService.register("test@example.com", "password123", "John", "Doe");

            assertThat(result).isNotNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getFirstName()).isEqualTo("John");
            assertThat(result.getLastName()).isEqualTo("Doe");
            verify(userRepository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw exception when email already exists")
        void shouldThrowWhenEmailExists() {
            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> authService.register("existing@example.com", "password123", "John", "Doe"))
                    .isInstanceOf(BusinessException.class)
                    .hasMessage("Email already registered");

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {

        private User activeUser;

        @BeforeEach
        void setup() {
            activeUser = new User("test@example.com", "hashed_password", "John", "Doe");
            activeUser.setId(UUID.randomUUID());
            activeUser.setActive(true);
        }

        @Test
        @DisplayName("Should login successfully with valid credentials")
        void shouldLoginSuccessfully() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("password123", "hashed_password")).thenReturn(true);
            when(userRepository.save(any(User.class))).thenReturn(activeUser);
            when(jwtService.generateAccessToken(any(), anyString(), any())).thenReturn("access_token");
            when(jwtService.generateRefreshTokenValue()).thenReturn("refresh_token");
            when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));

            AuthenticationUseCase.AuthResult result = authService.login("test@example.com", "password123", "127.0.0.1", "Chrome");

            assertThat(result.accessToken()).isEqualTo("access_token");
            assertThat(result.refreshToken()).isEqualTo("refresh_token");
            assertThat(result.user().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.login("unknown@example.com", "password", "127.0.0.1", "Chrome"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid credentials");
        }

        @Test
        @DisplayName("Should throw when password is wrong")
        void shouldThrowWhenPasswordWrong() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));
            when(passwordEncoder.matches("wrong_password", "hashed_password")).thenReturn(false);
            when(userRepository.save(any(User.class))).thenReturn(activeUser);

            assertThatThrownBy(() -> authService.login("test@example.com", "wrong_password", "127.0.0.1", "Chrome"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid credentials");
        }

        @Test
        @DisplayName("Should throw when account is locked")
        void shouldThrowWhenAccountLocked() {
            activeUser.setLockedUntil(Instant.now().plusSeconds(600));
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));

            assertThatThrownBy(() -> authService.login("test@example.com", "password123", "127.0.0.1", "Chrome"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("temporarily locked");
        }

        @Test
        @DisplayName("Should throw when account is deactivated")
        void shouldThrowWhenAccountDeactivated() {
            activeUser.setActive(false);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(activeUser));

            assertThatThrownBy(() -> authService.login("test@example.com", "password123", "127.0.0.1", "Chrome"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Account is deactivated");
        }
    }

    @Nested
    @DisplayName("Refresh Token")
    class RefreshTokenTests {

        @Test
        @DisplayName("Should refresh token successfully")
        void shouldRefreshTokenSuccessfully() {
            UUID userId = UUID.randomUUID();
            RefreshToken existingToken = new RefreshToken(userId, "old_token", Instant.now().plusSeconds(3600), null, null);
            existingToken.setId(UUID.randomUUID());

            User user = new User("test@example.com", "hash", "John", "Doe");
            user.setId(userId);
            user.setActive(true);

            when(refreshTokenRepository.findByToken("old_token")).thenReturn(Optional.of(existingToken));
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(i -> i.getArgument(0));
            when(jwtService.generateAccessToken(any(), anyString(), any())).thenReturn("new_access_token");
            when(jwtService.generateRefreshTokenValue()).thenReturn("new_refresh_token");
            when(jwtService.getRefreshTokenExpiration()).thenReturn(604800000L);

            AuthenticationUseCase.AuthResult result = authService.refreshToken("old_token");

            assertThat(result.accessToken()).isEqualTo("new_access_token");
            assertThat(result.refreshToken()).isEqualTo("new_refresh_token");
        }

        @Test
        @DisplayName("Should throw when refresh token is invalid")
        void shouldThrowWhenTokenInvalid() {
            when(refreshTokenRepository.findByToken("invalid_token")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> authService.refreshToken("invalid_token"))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessage("Invalid refresh token");
        }
    }

    @Nested
    @DisplayName("Logout")
    class LogoutTests {

        @Test
        @DisplayName("Should revoke refresh token on logout")
        void shouldRevokeTokenOnLogout() {
            RefreshToken token = new RefreshToken(UUID.randomUUID(), "token", Instant.now().plusSeconds(3600), null, null);
            when(refreshTokenRepository.findByToken("token")).thenReturn(Optional.of(token));
            when(refreshTokenRepository.save(any())).thenReturn(token);

            authService.logout("token");

            verify(refreshTokenRepository).save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("Should revoke all tokens on logoutAll")
        void shouldRevokeAllTokens() {
            UUID userId = UUID.randomUUID();

            authService.logoutAll(userId);

            verify(refreshTokenRepository).revokeAllByUserId(userId);
        }
    }
}
