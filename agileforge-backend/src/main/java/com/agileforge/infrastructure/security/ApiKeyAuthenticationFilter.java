package com.agileforge.infrastructure.security;

import com.agileforge.application.service.ApiKeyService;
import com.agileforge.domain.model.ApiKey;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private static final String API_KEY_PARAM = "api_key";

    private final ApiKeyService apiKeyService;

    public ApiKeyAuthenticationFilter(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKeyValue = extractApiKey(request);
        if (apiKeyValue != null) {
            ApiKey apiKey = apiKeyService.validateKey(apiKeyValue);
            if (apiKey != null) {
                List<SimpleGrantedAuthority> authorities = parsePermissions(apiKey.getPermissions());
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "api-key:" + apiKey.getKeyPrefix(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractApiKey(HttpServletRequest request) {
        String headerValue = request.getHeader(API_KEY_HEADER);
        if (headerValue != null && !headerValue.isBlank()) {
            return headerValue;
        }
        String paramValue = request.getParameter(API_KEY_PARAM);
        if (paramValue != null && !paramValue.isBlank()) {
            return paramValue;
        }
        return null;
    }

    private List<SimpleGrantedAuthority> parsePermissions(String permissions) {
        if (permissions == null || permissions.isBlank()) {
            return List.of(new SimpleGrantedAuthority("ROLE_API"));
        }
        return Arrays.stream(permissions.split(","))
                .map(String::trim)
                .filter(p -> !p.isBlank())
                .map(p -> new SimpleGrantedAuthority("ROLE_" + p.toUpperCase()))
                .toList();
    }
}
