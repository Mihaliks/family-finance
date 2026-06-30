package ru.mihaliks.finance.core.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.common.exception.BadRequestException;
import ru.mihaliks.finance.common.exception.ForbiddenException;
import ru.mihaliks.finance.common.exception.NotFoundException;
import ru.mihaliks.finance.core.api.FinanceDtos.SaveOperationRequest;
import ru.mihaliks.finance.core.domain.Category;
import ru.mihaliks.finance.core.domain.Operation;
import ru.mihaliks.finance.core.event.FinanceEventPublisher;
import ru.mihaliks.finance.core.repository.CategoryRepository;
import ru.mihaliks.finance.core.repository.OperationRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class OperationService {
    private final OperationRepository operations;
    private final CategoryRepository categories;
    private final FamilyService families;
    private final FinanceEventPublisher events;

    public OperationService(OperationRepository operations, CategoryRepository categories,
                            FamilyService families, FinanceEventPublisher events) {
        this.operations = operations;
        this.categories = categories;
        this.families = families;
        this.events = events;
    }

    public Mono<Operation> create(SaveOperationRequest request, UUID userId) {
        return validate(request, userId)
                .then(Mono.defer(() -> operations.save(new Operation(UUID.randomUUID(), userId, request.familyId(),
                        request.categoryId(), request.type().name(), request.amount(), request.operationDate(),
                        request.description(), Instant.now(), null))))
                .flatMap(saved -> events.publish("OPERATION", saved.id(), userId, saved.familyId(), "CREATED")
                        .thenReturn(saved));
    }

    public Mono<Operation> update(UUID id, SaveOperationRequest request, UUID userId) {
        return owned(id, userId)
                .flatMap(existing -> validate(request, userId)
                        .then(Mono.defer(() -> operations.save(new Operation(existing.id(), userId, request.familyId(),
                                request.categoryId(), request.type().name(), request.amount(), request.operationDate(),
                                request.description(), existing.createdAt(), existing.version())))))
                .flatMap(saved -> events.publish("OPERATION", saved.id(), userId, saved.familyId(), "UPDATED")
                        .thenReturn(saved));
    }

    public Mono<Void> delete(UUID id, UUID userId) {
        return owned(id, userId)
                .flatMap(operation -> operations.delete(operation)
                        .then(events.publish("OPERATION", operation.id(), userId, operation.familyId(), "DELETED")));
    }

    public Flux<Operation> listOwn(UUID userId) {
        return operations.findAllByUserIdOrderByOperationDateDesc(userId);
    }

    public Flux<Operation> report(UUID userId, UUID familyId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) {
            return Flux.error(new BadRequestException("Дата начала периода должна быть раньше даты окончания"));
        }
        if (familyId == null) {
            return operations.findAllByUserIdAndOperationDateBetweenOrderByOperationDateDesc(userId, from, to);
        }
        return families.requireMember(familyId, userId)
                .thenMany(operations.findAllByFamilyIdAndOperationDateBetweenOrderByOperationDateDesc(familyId, from, to));
    }

    private Mono<Void> validate(SaveOperationRequest request, UUID userId) {
        Mono<Category> category = categories.findById(request.categoryId())
                .switchIfEmpty(Mono.error(new NotFoundException("Категория не найдена")))
                .filter(value -> value.system() || userId.equals(value.ownerId()))
                .switchIfEmpty(Mono.error(new ForbiddenException("Нет доступа к категории")))
                .filter(value -> value.type().equals(request.type().name()))
                .switchIfEmpty(Mono.error(new BadRequestException("Тип категории не соответствует типу операции")));
        Mono<Void> family = request.familyId() == null
                ? Mono.empty()
                : families.requireMember(request.familyId(), userId);
        return category.then(family);
    }

    private Mono<Operation> owned(UUID id, UUID userId) {
        return operations.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Операция не найдена")))
                .filter(operation -> userId.equals(operation.userId()))
                .switchIfEmpty(Mono.error(new ForbiddenException("Изменять операцию может только ее автор")));
    }
}
