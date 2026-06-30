package ru.mihaliks.finance.common.event;

import java.time.Instant;
import java.util.UUID;

public record FinanceDataChangedEvent(
        UUID eventId,
        Instant occurredAt,
        String entityType,
        UUID entityId,
        UUID userId,
        UUID familyId,
        String action
) {
}
