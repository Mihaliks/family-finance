package ru.mihaliks.finance.core.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.common.exception.ForbiddenException;
import ru.mihaliks.finance.common.exception.ConflictException;
import ru.mihaliks.finance.common.exception.NotFoundException;
import ru.mihaliks.finance.core.api.FinanceDtos.CreateCategoryRequest;
import ru.mihaliks.finance.core.domain.Category;
import ru.mihaliks.finance.core.event.FinanceEventPublisher;
import ru.mihaliks.finance.core.repository.CategoryRepository;
import ru.mihaliks.finance.core.repository.OperationRepository;

import java.util.UUID;

@Service
public class CategoryService {
    private final CategoryRepository repository;
    private final OperationRepository operations;
    private final FinanceEventPublisher events;

    public CategoryService(CategoryRepository repository, OperationRepository operations, FinanceEventPublisher events) {
        this.repository = repository;
        this.operations = operations;
        this.events = events;
    }

    public Flux<Category> list(UUID userId) {
        return repository.findAvailable(userId);
    }

    public Mono<Category> create(CreateCategoryRequest request, UUID userId, boolean system) {
        Category category = new Category(UUID.randomUUID(), request.name().trim(), request.type().name(),
                system, system ? null : userId, null);
        return repository.save(category)
                .flatMap(saved -> events.publish("CATEGORY", saved.id(), userId, null, "CREATED").thenReturn(saved));
    }

    public Mono<Void> delete(UUID id, UUID userId, boolean admin) {
        return repository.findById(id)
                .switchIfEmpty(Mono.error(new NotFoundException("Категория не найдена")))
                .flatMap(category -> {
                    if (category.system() && !admin || !category.system() && !userId.equals(category.ownerId())) {
                        return Mono.error(new ForbiddenException("Нет прав для удаления категории"));
                    }
                    return operations.existsByCategoryId(category.id())
                            .flatMap(used -> used
                                    ? Mono.error(new ConflictException("Нельзя удалить используемую категорию"))
                                    : repository.delete(category)
                                    .then(events.publish("CATEGORY", category.id(), userId, null, "DELETED")));
                });
    }
}
