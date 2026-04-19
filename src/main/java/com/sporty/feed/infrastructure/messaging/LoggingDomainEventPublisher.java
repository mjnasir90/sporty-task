package com.sporty.feed.infrastructure.messaging;

import com.sporty.feed.application.gateway.DomainEventPublisher;
import com.sporty.feed.domain.event.DomainEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * In-memory (dev/stub) implementation of {@link DomainEventPublisher}.
 * Logs events to SLF4J only — no message broker involved.
 *
 * <p>To go to production, replace this with a broker-backed implementation
 * (e.g. {@code KafkaDomainEventPublisher}) that writes to a topic,
 * and register it as the active Spring bean (e.g. via {@code @Profile}).
 * No other code needs to change — all callers depend on the interface.
 */
@Slf4j
@Profile("local")
@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    @Override
    public void publish(DomainEvent event) {
        log.info("Domain event: {}", event);
    }
}
