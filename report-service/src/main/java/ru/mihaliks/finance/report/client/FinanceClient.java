package ru.mihaliks.finance.report.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.mihaliks.finance.common.exception.BadRequestException;
import ru.mihaliks.finance.common.exception.ExternalServiceException;
import ru.mihaliks.finance.common.exception.ForbiddenException;
import ru.mihaliks.finance.report.api.ReportDtos.OperationView;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class FinanceClient {
    private final WebClient webClient;

    public FinanceClient(WebClient.Builder builder, @Value("${app.finance-service-url}") String baseUrl) {
        this.webClient = builder.baseUrl(baseUrl).build();
    }

    public Flux<OperationView> operations(String token, LocalDate from, LocalDate to, UUID familyId) {
        return webClient.get()
                .uri(uri -> uri.path("/internal/reports/operations")
                        .queryParam("from", from)
                        .queryParam("to", to)
                        .queryParamIfPresent("familyId", java.util.Optional.ofNullable(familyId))
                        .build())
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(status -> status.value() == 400,
                        response -> Mono.error(new BadRequestException("Некорректные параметры отчета")))
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        response -> Mono.error(new ForbiddenException("Нет доступа к данным отчета")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ExternalServiceException("Финансовый сервис недоступен")))
                .bodyToFlux(OperationView.class)
                .retryWhen(retry());
    }

    public Mono<Void> checkAccess(String token, UUID familyId) {
        return webClient.get()
                .uri(uri -> uri.path("/internal/reports/access-check").queryParam("familyId", familyId).build())
                .headers(headers -> headers.setBearerAuth(token))
                .retrieve()
                .onStatus(status -> status.value() == 401 || status.value() == 403,
                        response -> Mono.error(new ForbiddenException("Нет доступа к семейному отчету")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ExternalServiceException("Финансовый сервис недоступен")))
                .bodyToMono(Void.class)
                .retryWhen(retry());
    }

    private Retry retry() {
        return Retry.backoff(3, Duration.ofMillis(150))
                .filter(error -> error instanceof ExternalServiceException
                        || error instanceof WebClientRequestException)
                .onRetryExhaustedThrow((spec, signal) ->
                        new ExternalServiceException("Финансовый сервис недоступен после повторных попыток"));
    }
}
