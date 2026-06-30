package ru.mihaliks.finance.core.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.core.domain.Operation;

import java.time.LocalDate;
import java.util.UUID;

public interface OperationRepository extends ReactiveCrudRepository<Operation, UUID> {
    Mono<Boolean> existsByCategoryId(UUID categoryId);
    Flux<Operation> findAllByUserIdOrderByOperationDateDesc(UUID userId);
    Flux<Operation> findAllByUserIdAndOperationDateBetweenOrderByOperationDateDesc(
            UUID userId, LocalDate from, LocalDate to);
    Flux<Operation> findAllByFamilyIdAndOperationDateBetweenOrderByOperationDateDesc(
            UUID familyId, LocalDate from, LocalDate to);
}
