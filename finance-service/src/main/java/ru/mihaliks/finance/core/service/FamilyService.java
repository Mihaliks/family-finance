package ru.mihaliks.finance.core.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.reactive.TransactionalOperator;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.common.exception.ConflictException;
import ru.mihaliks.finance.common.exception.ForbiddenException;
import ru.mihaliks.finance.common.exception.NotFoundException;
import ru.mihaliks.finance.core.client.AuthClient;
import ru.mihaliks.finance.core.domain.Family;
import ru.mihaliks.finance.core.domain.FamilyMember;
import ru.mihaliks.finance.core.event.FinanceEventPublisher;
import ru.mihaliks.finance.core.repository.FamilyMemberRepository;
import ru.mihaliks.finance.core.repository.FamilyRepository;

import java.time.Instant;
import java.util.UUID;

@Service
public class FamilyService {
    private final FamilyRepository families;
    private final FamilyMemberRepository members;
    private final AuthClient authClient;
    private final TransactionalOperator tx;
    private final FinanceEventPublisher events;

    public FamilyService(FamilyRepository families, FamilyMemberRepository members, AuthClient authClient,
                         TransactionalOperator tx, FinanceEventPublisher events) {
        this.families = families;
        this.members = members;
        this.authClient = authClient;
        this.tx = tx;
        this.events = events;
    }

    public Mono<Family> create(String name, UUID ownerId) {
        Family family = new Family(UUID.randomUUID(), name.trim(), ownerId, Instant.now(), null);
        Mono<Family> work = families.save(family)
                .flatMap(saved -> members.save(new FamilyMember(UUID.randomUUID(), saved.id(), ownerId,
                                "OWNER", Instant.now(), null))
                        .thenReturn(saved));
        return tx.transactional(work);
    }

    public Flux<Family> list(UUID userId) {
        return members.findAllByUserId(userId).flatMap(member -> families.findById(member.familyId()));
    }

    public Flux<FamilyMember> members(UUID familyId, UUID userId) {
        return requireMember(familyId, userId).thenMany(members.findAllByFamilyId(familyId));
    }

    public Mono<FamilyMember> addMember(UUID familyId, UUID requesterId, String email) {
        return requireOwner(familyId, requesterId)
                .then(Mono.defer(() -> authClient.findByEmail(email)))
                .flatMap(user -> members.existsByFamilyIdAndUserId(familyId, user.id())
                        .flatMap(exists -> exists
                                ? Mono.error(new ConflictException("Пользователь уже состоит в семье"))
                                : members.save(new FamilyMember(UUID.randomUUID(), familyId, user.id(),
                                "MEMBER", Instant.now(), null))))
                .flatMap(member -> events.publish("FAMILY_MEMBER", member.id(), member.userId(), familyId, "CREATED")
                        .thenReturn(member));
    }

    public Mono<Void> removeMember(UUID familyId, UUID requesterId, UUID memberId) {
        return requireOwner(familyId, requesterId)
                .then(members.findById(memberId)
                        .filter(member -> member.familyId().equals(familyId))
                        .switchIfEmpty(Mono.error(new NotFoundException("Участник семьи не найден"))))
                .flatMap(member -> {
                    if ("OWNER".equals(member.role())) {
                        return Mono.error(new ConflictException("Нельзя удалить владельца семьи"));
                    }
                    return members.delete(member)
                            .then(events.publish("FAMILY_MEMBER", member.id(), member.userId(), familyId, "DELETED"));
                });
    }

    public Mono<Void> requireMember(UUID familyId, UUID userId) {
        return members.existsByFamilyIdAndUserId(familyId, userId)
                .flatMap(exists -> exists ? Mono.empty() : Mono.error(new ForbiddenException("Нет доступа к семье")));
    }

    private Mono<Void> requireOwner(UUID familyId, UUID userId) {
        return families.findById(familyId)
                .switchIfEmpty(Mono.error(new NotFoundException("Семья не найдена")))
                .filter(family -> family.ownerId().equals(userId))
                .switchIfEmpty(Mono.error(new ForbiddenException("Только владелец может управлять участниками")))
                .then();
    }
}
