package com.sporty.feed.application.service;

import com.sporty.feed.application.gateway.DomainEventPublisher;
import com.sporty.feed.application.usecase.OddsChangeUseCase;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.domain.event.OddsChangedEvent;
import com.sporty.feed.domain.model.OddsChange;
import lombok.extern.slf4j.Slf4j;

/**
 * Interactor for the odds-change use case.
 * Pure Java — no framework dependencies. Wired as a @Bean in ApplicationConfig.
 */
@Slf4j
public class OddsChangeInteractor implements OddsChangeUseCase {

    private final DomainEventPublisher domainEventPublisher;

    public OddsChangeInteractor(DomainEventPublisher domainEventPublisher) {
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public void execute(OddsChangeCommand command) {
        OddsChange oddsChange = new OddsChange(
                command.eventId(), command.timestamp(),
                command.homeOdds(), command.drawOdds(), command.awayOdds());
        log.info("[QUEUE] ODDS_CHANGE eventId={} home={} draw={} away={} timestamp={}",
                oddsChange.eventId(), oddsChange.homeOdds(), oddsChange.drawOdds(), oddsChange.awayOdds(), oddsChange.timestamp());
        domainEventPublisher.publish(OddsChangedEvent.from(oddsChange));
    }
}
