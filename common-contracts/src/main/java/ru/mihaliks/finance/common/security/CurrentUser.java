package ru.mihaliks.finance.common.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.UUID;

public record CurrentUser(UUID id, String email, String role, String token) {
    public static CurrentUser from(Jwt jwt) {
        return new CurrentUser(
                UUID.fromString(jwt.getSubject()),
                jwt.getClaimAsString("email"),
                jwt.getClaimAsString("role"),
                jwt.getTokenValue()
        );
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
