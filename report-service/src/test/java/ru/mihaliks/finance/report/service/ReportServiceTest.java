package ru.mihaliks.finance.report.service;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.mihaliks.finance.report.api.ReportDtos.GroupBy;
import ru.mihaliks.finance.report.api.ReportDtos.OperationView;
import ru.mihaliks.finance.report.client.FinanceClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceTest {
    private final FinanceClient client = Mockito.mock(FinanceClient.class);
    private final ReportService service = new ReportService(client, Duration.ofMinutes(1));

    @Test
    void calculatesTotalsAndCachesReport() {
        UUID userId = UUID.randomUUID();
        LocalDate day = LocalDate.now();
        when(client.operations("token", day, day, null)).thenReturn(Flux.just(
                operation(userId, "INCOME", "1000"),
                operation(userId, "EXPENSE", "250")
        ));

        StepVerifier.create(service.build(userId, "token", day, day, null, Set.of(), null, GroupBy.TYPE))
                .expectNextMatches(report -> report.balance().compareTo(new BigDecimal("750")) == 0
                        && report.groups().size() == 2)
                .verifyComplete();
        StepVerifier.create(service.build(userId, "token", day, day, null, Set.of(), null, GroupBy.TYPE))
                .expectNextCount(1)
                .verifyComplete();

        verify(client, times(1)).operations("token", day, day, null);
    }

    @Test
    void clearsCache() {
        UUID userId = UUID.randomUUID();
        LocalDate day = LocalDate.now();
        when(client.operations("token", day, day, null)).thenReturn(Flux.empty());
        service.build(userId, "token", day, day, null, Set.of(), null, GroupBy.NONE).block();
        service.clearCache();
        assert service.cacheSize() == 0;
    }

    private OperationView operation(UUID userId, String type, String amount) {
        return new OperationView(UUID.randomUUID(), userId, null, UUID.randomUUID(), type,
                new BigDecimal(amount), LocalDate.now(), "", Instant.now());
    }
}
