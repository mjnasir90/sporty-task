package com.sporty.feed.infrastructure.messaging;

import com.sporty.feed.application.gateway.DomainEventPublisher;
import com.sporty.feed.domain.event.DomainEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingDomainEventPublisher implements DomainEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        log.info("Domain event: {}", event);
    }
}
