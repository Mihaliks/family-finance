package ru.mihaliks.finance.core.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("families")
public record Family(@Id UUID id, String name, UUID ownerId, Instant createdAt, @Version Long version) {
}
