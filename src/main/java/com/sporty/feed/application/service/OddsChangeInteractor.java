package com.sporty.feed.application.service;

import com.sporty.feed.application.gateway.DomainEventPublisher;
import com.sporty.feed.application.usecase.OddsChangeUseCase;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.domain.event.OddsChangedEvent;
import com.sporty.feed.domain.model.OddsChangeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Interactor for the odds-change use case.
 * Pure Java — no framework dependencies. Wired as a @Bean in ApplicationConfig.
 */
public class OddsChangeInteractor implements OddsChangeUseCase {

    private static final Logger log = LoggerFactory.getLogger(OddsChangeInteractor.class);

    private final DomainEventPublisher domainEventPublisher;

    public OddsChangeInteractor(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void execute(OddsChangeCommand command) {
        OddsChangeMessage message = new OddsChangeMessage(
                command.eventId(), command.timestamp(),
                command.homeOdds(), command.drawOdds(), command.awayOdds());
        log.info("[QUEUE] ODDS_CHANGE eventId={} home={} draw={} away={} timestamp={}",
                message.eventId(), message.homeOdds(), message.drawOdds(), message.awayOdds(), message.timestamp());
        domainEventPublisher.publish(OddsChangedEvent.from(message));
    }
}
