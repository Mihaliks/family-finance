package ru.mihaliks.finance.core.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Table("operations")
public record Operation(
        @Id UUID id,
        UUID userId,
        UUID familyId,
        UUID categoryId,
        String type,
        BigDecimal amount,
        LocalDate operationDate,
        String description,
        Instant createdAt,
        @Version Long version
) {
}
