package ru.mihaliks.finance.auth.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.auth.domain.UserAccount;

import java.util.UUID;

public interface UserRepository extends ReactiveCrudRepository<UserAccount, UUID> {
    Mono<UserAccount> findByEmail(String email);
    Mono<Boolean> existsByEmail(String email);
}
