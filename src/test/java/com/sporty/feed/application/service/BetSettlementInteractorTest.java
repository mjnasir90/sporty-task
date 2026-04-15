package com.sporty.feed.application.service;

import com.sporty.feed.application.gateway.DomainEventPublisher;
import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.domain.event.BetSettledEvent;
import com.sporty.feed.domain.model.Outcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BetSettlementInteractorTest {

    @Mock
    DomainEventPublisher domainEventPublisher;

    BetSettlementInteractor interactor;

    @BeforeEach
    void setUp() {
        interactor = new BetSettlementInteractor(domainEventPublisher);
    }

    @Test
    void execute_publishesDomainEvent() {
        var timestamp = Instant.now();
        var command = new BetSettlementCommand("ev123", timestamp, Outcome.HOME);

        interactor.execute(command);

        verify(domainEventPublisher).publish(new BetSettledEvent("ev123", timestamp, Outcome.HOME));
    }
}
