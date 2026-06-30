package ru.mihaliks.finance.core.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import reactor.test.StepVerifier;
import ru.mihaliks.finance.core.domain.Operation;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@SpringBootTest(properties = "spring.kafka.bootstrap-servers=localhost:1")
@Testcontainers(disabledWithoutDocker = true)
class OperationRepositoryIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("finance_db")
            .withUsername("finance")
            .withPassword("finance");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () -> "r2dbc:postgresql://" + POSTGRES.getHost() + ":"
                + POSTGRES.getMappedPort(5432) + "/" + POSTGRES.getDatabaseName());
        registry.add("spring.r2dbc.username", POSTGRES::getUsername);
        registry.add("spring.r2dbc.password", POSTGRES::getPassword);
        registry.add("spring.flyway.url", POSTGRES::getJdbcUrl);
        registry.add("spring.flyway.user", POSTGRES::getUsername);
        registry.add("spring.flyway.password", POSTGRES::getPassword);
    }

    @Autowired
    OperationRepository repository;

    @Test
    void storesAndFindsOperationByUserAndPeriod() {
        UUID userId = UUID.randomUUID();
        Operation operation = new Operation(UUID.randomUUID(), userId, null,
                UUID.fromString("10000000-0000-0000-0000-000000000001"), "EXPENSE",
                new BigDecimal("125.50"), LocalDate.now(), "Продукты", Instant.now(), null);

        StepVerifier.create(repository.save(operation)
                        .thenMany(repository.findAllByUserIdAndOperationDateBetweenOrderByOperationDateDesc(
                                userId, LocalDate.now().minusDays(1), LocalDate.now())))
                .expectNextMatches(saved -> saved.amount().compareTo(new BigDecimal("125.50")) == 0)
                .verifyComplete();
    }
}
