package ru.mihaliks.finance.report.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.common.exception.BadRequestException;
import ru.mihaliks.finance.report.api.ReportDtos.GroupBy;
import ru.mihaliks.finance.report.api.ReportDtos.OperationView;
import ru.mihaliks.finance.report.api.ReportDtos.ReportResponse;
import ru.mihaliks.finance.report.client.FinanceClient;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final FinanceClient financeClient;
    private final Cache<ReportKey, ReportResponse> cache;

    public ReportService(FinanceClient financeClient, @Value("${app.cache-ttl:PT1M}") Duration ttl) {
        this.financeClient = financeClient;
        this.cache = Caffeine.newBuilder().maximumSize(500).expireAfterWrite(ttl).build();
    }

    public Mono<ReportResponse> build(UUID userId, String token, LocalDate from, LocalDate to, UUID familyId,
                                      Set<UUID> memberIds, String type, GroupBy groupBy) {
        if (from.isAfter(to)) {
            return Mono.error(new BadRequestException("Дата начала периода должна быть раньше даты окончания"));
        }
        if (type != null && !type.isBlank()
                && !"INCOME".equalsIgnoreCase(type) && !"EXPENSE".equalsIgnoreCase(type)) {
            return Mono.error(new BadRequestException("Тип операции должен быть INCOME или EXPENSE"));
        }
        ReportKey key = new ReportKey(userId, from, to, familyId, memberIds, type, groupBy);
        Mono<Void> accessCheck = familyId == null ? Mono.empty() : financeClient.checkAccess(token, familyId);
        return accessCheck.then(Mono.defer(() -> {
            ReportResponse cached = cache.getIfPresent(key);
            if (cached != null) {
                return Mono.just(cached);
            }
            return financeClient.operations(token, from, to, familyId)
                    .filter(operation -> memberIds == null || memberIds.isEmpty() || memberIds.contains(operation.userId()))
                    .filter(operation -> type == null || type.isBlank() || type.equalsIgnoreCase(operation.type()))
                    .collectList()
                    .map(operations -> calculate(from, to, operations, groupBy))
                    .doOnNext(report -> cache.put(key, report));
        }));
    }

    public void clearCache() {
        cache.invalidateAll();
    }

    int cacheSize() {
        return Math.toIntExact(cache.estimatedSize());
    }

    private ReportResponse calculate(LocalDate from, LocalDate to, List<OperationView> operations, GroupBy groupBy) {
        BigDecimal income = sum(operations, "INCOME");
        BigDecimal expense = sum(operations, "EXPENSE");
        Map<String, BigDecimal> groups = group(operations, groupBy);
        List<OperationView> sorted = operations.stream()
                .sorted(Comparator.comparing(OperationView::operationDate).reversed())
                .toList();
        return new ReportResponse(from, to, income, expense, income.subtract(expense), groups, sorted);
    }

    private BigDecimal sum(List<OperationView> operations, String type) {
        return operations.stream()
                .filter(operation -> type.equals(operation.type()))
                .map(OperationView::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private Map<String, BigDecimal> group(List<OperationView> operations, GroupBy groupBy) {
        if (groupBy == GroupBy.NONE) {
            return Map.of();
        }
        Function<OperationView, String> classifier = switch (groupBy) {
            case CATEGORY -> operation -> operation.categoryId().toString();
            case USER -> operation -> operation.userId().toString();
            case TYPE -> OperationView::type;
            case NONE -> ignored -> "";
        };
        return operations.stream().collect(Collectors.groupingBy(classifier, LinkedHashMap::new,
                Collectors.reducing(BigDecimal.ZERO, OperationView::amount, BigDecimal::add)));
    }

    private record ReportKey(UUID userId, LocalDate from, LocalDate to, UUID familyId,
                             Set<UUID> memberIds, String type, GroupBy groupBy) {
        private ReportKey {
            memberIds = memberIds == null ? Set.of() : Set.copyOf(memberIds);
            type = type == null ? "" : type.toUpperCase();
        }
    }
}
