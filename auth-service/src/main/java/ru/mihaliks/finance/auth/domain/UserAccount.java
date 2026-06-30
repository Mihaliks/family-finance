package ru.mihaliks.finance.auth.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("users")
public record UserAccount(
        @Id UUID id,
        String email,
        String passwordHash,
        String role,
        Instant createdAt,
        @Version Long version
) {
}
