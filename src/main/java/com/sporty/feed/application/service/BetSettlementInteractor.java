package com.sporty.feed.application.service;

import com.sporty.feed.application.gateway.DomainEventPublisher;
import com.sporty.feed.application.usecase.BetSettlementUseCase;
import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.domain.event.BetSettledEvent;
import com.sporty.feed.domain.model.BetSettlementMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interactor for the bet-settlement use case.
 * Pure Java — no framework dependencies. Wired as a @Bean in ApplicationConfig.
 */
public class BetSettlementInteractor implements BetSettlementUseCase {

    private static final Logger log = LoggerFactory.getLogger(BetSettlementInteractor.class);

    private final DomainEventPublisher domainEventPublisher;

    public BetSettlementInteractor(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void execute(BetSettlementCommand command) {
        BetSettlementMessage message = new BetSettlementMessage(
                command.eventId(), command.timestamp(), command.outcome());
        log.info("[QUEUE] BET_SETTLEMENT eventId={} outcome={} timestamp={}",
                message.eventId(), message.outcome(), message.timestamp());
        domainEventPublisher.publish(new BetSettledEvent(
                command.eventId(), command.timestamp(), command.outcome()));
    }
}
