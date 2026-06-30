package ru.mihaliks.finance.report.api;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class ReportDtos {
    private ReportDtos() {
    }

    public enum GroupBy { NONE, CATEGORY, USER, TYPE }

    public record OperationView(
            UUID id, UUID userId, UUID familyId, UUID categoryId, String type, BigDecimal amount,
            LocalDate operationDate, String description, Instant createdAt
    ) {
    }

    public record ReportResponse(
            LocalDate from,
            LocalDate to,
            BigDecimal totalIncome,
            BigDecimal totalExpense,
            BigDecimal balance,
            Map<String, BigDecimal> groups,
            List<OperationView> operations
    ) {
    }

    public record UiConfig(String authServiceUrl) {
    }
}
