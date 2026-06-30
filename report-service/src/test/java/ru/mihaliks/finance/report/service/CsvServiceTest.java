package ru.mihaliks.finance.report.service;

import org.junit.jupiter.api.Test;
import ru.mihaliks.finance.report.api.ReportDtos.OperationView;
import ru.mihaliks.finance.report.api.ReportDtos.ReportResponse;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CsvServiceTest {
    @Test
    void escapesQuotesInDescription() {
        OperationView operation = new OperationView(UUID.randomUUID(), UUID.randomUUID(), null, UUID.randomUUID(),
                "EXPENSE", BigDecimal.TEN, LocalDate.now(), "Say \"hello\"", Instant.now());
        ReportResponse report = new ReportResponse(LocalDate.now(), LocalDate.now(), BigDecimal.ZERO, BigDecimal.TEN,
                BigDecimal.TEN.negate(), Map.of(), List.of(operation));

        assertThat(new CsvService().create(report)).contains("\"Say \"\"hello\"\"\"");
    }
}
