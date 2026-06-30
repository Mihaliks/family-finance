package ru.mihaliks.finance.core.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mihaliks.finance.common.exception.ConflictException;
import ru.mihaliks.finance.common.exception.ForbiddenException;
import ru.mihaliks.finance.core.api.FinanceDtos.UserLookupResponse;
import ru.mihaliks.finance.core.client.AuthClient;
import ru.mihaliks.finance.core.domain.Family;
import ru.mihaliks.finance.core.domain.FamilyMember;
import ru.mihaliks.finance.core.event.FinanceEventPublisher;
import ru.mihaliks.finance.core.repository.FamilyMemberRepository;
import ru.mihaliks.finance.core.repository.FamilyRepository;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class FamilyServiceTest {
    private final FamilyRepository families = Mockito.mock(FamilyRepository.class);
    private final FamilyMemberRepository members = Mockito.mock(FamilyMemberRepository.class);
    private final AuthClient authClient = Mockito.mock(AuthClient.class);
    private final TransactionalOperator tx = Mockito.mock(TransactionalOperator.class);
    private final FinanceEventPublisher events = Mockito.mock(FinanceEventPublisher.class);
    private final FamilyService service = new FamilyService(families, members, authClient, tx, events);

    @Test
    void createsFamilyWithOwner() {
        UUID ownerId = UUID.randomUUID();
        when(families.save(any())).thenAnswer(call -> Mono.just(call.getArgument(0)));
        when(members.save(any())).thenAnswer(call -> Mono.just(call.getArgument(0)));
        when(tx.transactional(Mockito.<Mono<Family>>any())).thenAnswer(call -> call.getArgument(0));

        StepVerifier.create(service.create("Family", ownerId))
                .expectNextMatches(family -> family.ownerId().equals(ownerId) && family.name().equals("Family"))
                .verifyComplete();
    }

    @Test
    void rejectsExistingMember() {
        UUID ownerId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        UUID memberId = UUID.randomUUID();
        when(families.findById(familyId))
                .thenReturn(Mono.just(new Family(familyId, "Family", ownerId, Instant.now(), 0L)));
        when(authClient.findByEmail("member@example.com"))
                .thenReturn(Mono.just(new UserLookupResponse(memberId, "member@example.com", "USER")));
        when(members.existsByFamilyIdAndUserId(familyId, memberId)).thenReturn(Mono.just(true));

        StepVerifier.create(service.addMember(familyId, ownerId, "member@example.com"))
                .expectError(ConflictException.class)
                .verify();
    }

    @Test
    void nonOwnerCannotAddMember() {
        UUID ownerId = UUID.randomUUID();
        UUID requesterId = UUID.randomUUID();
        UUID familyId = UUID.randomUUID();
        when(families.findById(familyId))
                .thenReturn(Mono.just(new Family(familyId, "Family", ownerId, Instant.now(), 0L)));

        StepVerifier.create(service.addMember(familyId, requesterId, "member@example.com"))
                .expectError(ForbiddenException.class)
                .verify();
    }
}
