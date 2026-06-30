package ru.mihaliks.finance.core.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;
import ru.mihaliks.finance.common.exception.ExternalServiceException;
import ru.mihaliks.finance.common.exception.NotFoundException;
import ru.mihaliks.finance.core.api.FinanceDtos.UserLookupResponse;

import java.time.Duration;

@Component
public class AuthClient {
    private final WebClient webClient;
    private final String internalApiKey;

    public AuthClient(
            WebClient.Builder builder,
            @Value("${app.auth-service-url}") String baseUrl,
            @Value("${app.internal-api-key}") String internalApiKey
    ) {
        this.webClient = builder.baseUrl(baseUrl).build();
        this.internalApiKey = internalApiKey;
    }

    public Mono<UserLookupResponse> findByEmail(String email) {
        return webClient.get()
                .uri(uri -> uri.path("/internal/users/by-email").queryParam("email", email).build())
                .header("X-Internal-Api-Key", internalApiKey)
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(new NotFoundException("Пользователь с таким email не найден")))
                .onStatus(HttpStatusCode::is5xxServerError,
                        response -> Mono.error(new ExternalServiceException("Сервис авторизации недоступен")))
                .bodyToMono(UserLookupResponse.class)
                .retryWhen(Retry.backoff(3, Duration.ofMillis(150))
                        .filter(error -> error instanceof ExternalServiceException
                                || error instanceof WebClientRequestException))
                .onErrorMap(error -> !(error instanceof NotFoundException)
                                && !(error instanceof ExternalServiceException),
                        error -> new ExternalServiceException("Ошибка связи с сервисом авторизации"));
    }
}
