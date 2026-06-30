package ru.mihaliks.finance.core.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

@Table("family_members")
public record FamilyMember(@Id UUID id, UUID familyId, UUID userId, String role, Instant joinedAt,
                           @Version Long version) {
}
