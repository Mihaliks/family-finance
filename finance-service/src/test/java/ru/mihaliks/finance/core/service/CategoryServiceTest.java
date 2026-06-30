package ru.mihaliks.finance.core.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mihaliks.finance.common.exception.ForbiddenException;
import ru.mihaliks.finance.common.exception.ConflictException;
import ru.mihaliks.finance.core.domain.Category;
import ru.mihaliks.finance.core.event.FinanceEventPublisher;
import ru.mihaliks.finance.core.repository.CategoryRepository;
import ru.mihaliks.finance.core.repository.OperationRepository;

import java.util.UUID;

import static org.mockito.Mockito.when;

class CategoryServiceTest {
    private final CategoryRepository repository = Mockito.mock(CategoryRepository.class);
    private final OperationRepository operations = Mockito.mock(OperationRepository.class);
    private final FinanceEventPublisher events = Mockito.mock(FinanceEventPublisher.class);
    private final CategoryService service = new CategoryService(repository, operations, events);

    @Test
    void userCannotDeleteSystemCategory() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Mono.just(new Category(id, "Food", "EXPENSE", true, null, 0L)));

        StepVerifier.create(service.delete(id, UUID.randomUUID(), false))
                .expectError(ForbiddenException.class)
                .verify();
    }

    @Test
    void cannotDeleteUsedOwnCategory() {
        UUID id = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(repository.findById(id))
                .thenReturn(Mono.just(new Category(id, "Custom", "EXPENSE", false, userId, 0L)));
        when(operations.existsByCategoryId(id)).thenReturn(Mono.just(true));

        StepVerifier.create(service.delete(id, userId, false))
                .expectError(ConflictException.class)
                .verify();
    }
}
