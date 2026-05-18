package com.agileforge.domain.port.in;

import com.agileforge.domain.model.User;

public interface AuthenticationUseCase {

    AuthResult login(String email, String password, String ipAddress, String userAgent);

    AuthResult refreshToken(String refreshToken);

    void logout(String refreshToken);

    void logoutAll(java.util.UUID userId);

    record AuthResult(User user, String accessToken, String refreshToken) {}
}
