package ru.mihaliks.finance.core.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("categories")
public record Category(@Id UUID id, String name, String type, boolean system, UUID ownerId, @Version Long version) {
}
