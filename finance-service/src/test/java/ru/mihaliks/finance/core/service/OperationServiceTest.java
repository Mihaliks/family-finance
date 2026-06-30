package ru.mihaliks.finance.core.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mihaliks.finance.common.exception.BadRequestException;
import ru.mihaliks.finance.common.exception.ForbiddenException;
import ru.mihaliks.finance.core.api.FinanceDtos.OperationType;
import ru.mihaliks.finance.core.api.FinanceDtos.SaveOperationRequest;
import ru.mihaliks.finance.core.domain.Category;
import ru.mihaliks.finance.core.event.FinanceEventPublisher;
import ru.mihaliks.finance.core.repository.CategoryRepository;
import ru.mihaliks.finance.core.repository.OperationRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class OperationServiceTest {
    private final OperationRepository operations = Mockito.mock(OperationRepository.class);
    private final CategoryRepository categories = Mockito.mock(CategoryRepository.class);
    private final FamilyService families = Mockito.mock(FamilyService.class);
    private final FinanceEventPublisher events = Mockito.mock(FinanceEventPublisher.class);
    private final OperationService service = new OperationService(operations, categories, families, events);

    @Test
    void createsValidOperation() {
        UUID userId = UUID.randomUUID();
        UUID categoryId = UUID.randomUUID();
        SaveOperationRequest request = request(categoryId, OperationType.EXPENSE);
        when(categories.findById(categoryId))
                .thenReturn(Mono.just(new Category(categoryId, "Food", "EXPENSE", true, null, 0L)));
        when(operations.save(any())).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));
        when(events.publish(any(), any(), any(), any(), any())).thenReturn(Mono.empty());

        StepVerifier.create(service.create(request, userId))
                .expectNextMatches(operation -> operation.userId().equals(userId))
                .verifyComplete();
    }

    @Test
    void rejectsCategoryOfWrongType() {
        UUID categoryId = UUID.randomUUID();
        when(categories.findById(categoryId))
                .thenReturn(Mono.just(new Category(categoryId, "Salary", "INCOME", true, null, 0L)));

        StepVerifier.create(service.create(request(categoryId, OperationType.EXPENSE), UUID.randomUUID()))
                .expectError(BadRequestException.class)
                .verify();
    }

    @Test
    void rejectsForeignPersonalCategory() {
        UUID categoryId = UUID.randomUUID();
        when(categories.findById(categoryId))
                .thenReturn(Mono.just(new Category(categoryId, "Private", "EXPENSE", false, UUID.randomUUID(), 0L)));

        StepVerifier.create(service.create(request(categoryId, OperationType.EXPENSE), UUID.randomUUID()))
                .expectError(ForbiddenException.class)
                .verify();
    }

    private SaveOperationRequest request(UUID categoryId, OperationType type) {
        return new SaveOperationRequest(categoryId, null, type, new BigDecimal("100.00"),
                LocalDate.now(), "test");
    }
}
