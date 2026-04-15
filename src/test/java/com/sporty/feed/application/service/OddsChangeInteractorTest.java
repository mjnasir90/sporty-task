package com.sporty.feed.application.service;

import com.sporty.feed.application.gateway.DomainEventPublisher;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.domain.event.OddsChangedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OddsChangeInteractorTest {

    @Mock
    DomainEventPublisher domainEventPublisher;

    OddsChangeInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new OddsChangeInteractor(domainEventPublisher);
    }

    @Test
    void execute_publishesDomainEvent() {
        var timestamp = Instant.now();
        var command = new OddsChangeCommand("ev123", timestamp, 2.0, 3.1, 3.8);

        interactor.execute(command);

        verify(domainEventPublisher).publish(new OddsChangedEvent("ev123", timestamp, 2.0, 3.1, 3.8));
    }
}
