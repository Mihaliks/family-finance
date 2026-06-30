package ru.mihaliks.finance.core.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.mihaliks.finance.common.event.FinanceDataChangedEvent;

import java.time.Instant;
import java.util.UUID;

@Component
public class FinanceEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(FinanceEventPublisher.class);
    private final KafkaTemplate<String, FinanceDataChangedEvent> kafkaTemplate;

    public FinanceEventPublisher(KafkaTemplate<String, FinanceDataChangedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Mono<Void> publish(String entityType, UUID entityId, UUID userId, UUID familyId, String action) {
        FinanceDataChangedEvent event = new FinanceDataChangedEvent(
                UUID.randomUUID(), Instant.now(), entityType, entityId, userId, familyId, action);
        return Mono.fromFuture(kafkaTemplate.send("finance-data-changed", entityId.toString(), event))
                .then()
                .onErrorResume(error -> {
                    log.warn("Kafka event was not published: {}", error.getMessage());
                    return Mono.empty();
                });
    }
}
