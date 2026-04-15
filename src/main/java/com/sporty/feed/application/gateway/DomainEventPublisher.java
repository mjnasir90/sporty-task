package com.sporty.feed.application.gateway;

import com.sporty.feed.domain.event.DomainEvent;

/**
 * Output port for publishing domain events.
 * Declared in the application layer; infrastructure provides the implementation.
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);
}
