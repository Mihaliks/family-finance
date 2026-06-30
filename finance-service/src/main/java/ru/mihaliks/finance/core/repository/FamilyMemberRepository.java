package ru.mihaliks.finance.core.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.core.domain.FamilyMember;

import java.util.UUID;

public interface FamilyMemberRepository extends ReactiveCrudRepository<FamilyMember, UUID> {
    Mono<Boolean> existsByFamilyIdAndUserId(UUID familyId, UUID userId);
    Mono<FamilyMember> findByFamilyIdAndUserId(UUID familyId, UUID userId);
    Flux<FamilyMember> findAllByFamilyId(UUID familyId);

    @Query("SELECT fm.* FROM family_members fm WHERE fm.user_id = :userId ORDER BY fm.joined_at")
    Flux<FamilyMember> findAllByUserId(UUID userId);
}
