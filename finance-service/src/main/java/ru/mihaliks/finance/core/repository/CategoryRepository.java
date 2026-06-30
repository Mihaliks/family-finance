package ru.mihaliks.finance.core.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import ru.mihaliks.finance.core.domain.Category;

import java.util.UUID;

public interface CategoryRepository extends ReactiveCrudRepository<Category, UUID> {
    @Query("SELECT * FROM categories WHERE system = TRUE OR owner_id = :userId ORDER BY type, name")
    Flux<Category> findAvailable(UUID userId);
}
