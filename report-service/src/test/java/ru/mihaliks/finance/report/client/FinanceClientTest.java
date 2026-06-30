package ru.mihaliks.finance.report.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.mihaliks.finance.common.exception.ForbiddenException;

import java.time.LocalDate;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class FinanceClientTest {
    @Test
    void retriesServerErrorsThenSucceeds() {
        AtomicInteger calls = new AtomicInteger();
        ExchangeFunction exchange = request -> {
            if (calls.incrementAndGet() < 3) {
                return Mono.just(ClientResponse.create(HttpStatus.SERVICE_UNAVAILABLE).build());
            }
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header("Content-Type", "application/json")
                    .body("[]")
                    .build());
        };
        FinanceClient client = new FinanceClient(WebClient.builder().exchangeFunction(exchange), "http://finance");

        StepVerifier.create(client.operations("token", LocalDate.now(), LocalDate.now(), null).collectList())
                .expectNextMatches(java.util.List::isEmpty)
                .verifyComplete();

        assertThat(calls).hasValue(3);
    }

    @Test
    void doesNotRetryForbiddenResponse() {
        AtomicInteger calls = new AtomicInteger();
        ExchangeFunction exchange = request -> {
            calls.incrementAndGet();
            return Mono.just(ClientResponse.create(HttpStatus.FORBIDDEN).build());
        };
        FinanceClient client = new FinanceClient(WebClient.builder().exchangeFunction(exchange), "http://finance");

        StepVerifier.create(client.checkAccess("token", java.util.UUID.randomUUID()))
                .expectError(ForbiddenException.class)
                .verify();

        assertThat(calls).hasValue(1);
    }
}
