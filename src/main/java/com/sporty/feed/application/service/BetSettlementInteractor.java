package com.sporty.feed.application.service;

import com.sporty.feed.application.gateway.DomainEventPublisher;
import com.sporty.feed.application.usecase.BetSettlementUseCase;
import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.domain.event.BetSettledEvent;
import com.sporty.feed.domain.model.BetSettlement;
import lombok.extern.slf4j.Slf4j;

/**
 * Interactor for the bet-settlement use case.
 * Pure Java — no framework dependencies. Wired as a @Bean in ApplicationConfig.
 */
@Slf4j
public class BetSettlementInteractor implements BetSettlementUseCase {

    private final DomainEventPublisher domainEventPublisher;

    public BetSettlementInteractor(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void execute(BetSettlementCommand command) {
        BetSettlement betSettlement = new BetSettlement(
                command.eventId(), command.timestamp(), command.outcome());
        log.info("[QUEUE] BET_SETTLEMENT eventId={} outcome={} timestamp={}",
                betSettlement.eventId(), betSettlement.outcome(), betSettlement.timestamp());
        domainEventPublisher.publish(BetSettledEvent.from(betSettlement));
    }
}
