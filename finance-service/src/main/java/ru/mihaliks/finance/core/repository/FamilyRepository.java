package ru.mihaliks.finance.core.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import ru.mihaliks.finance.core.domain.Family;

import java.util.UUID;

public interface FamilyRepository extends ReactiveCrudRepository<Family, UUID> {
}
