package com.sporty.feed.domain.event;

import com.sporty.feed.domain.model.OddsChange;

import java.time.Instant;

public record OddsChangedEvent(
        String eventId,
        Instant timestamp,
        double homeOdds,
        double drawOdds,
        double awayOdds
) implements DomainEvent {

    public static OddsChangedEvent from(OddsChange oddsChange) {
        return new OddsChangedEvent(
                oddsChange.eventId(), oddsChange.timestamp(),
                oddsChange.homeOdds(), oddsChange.drawOdds(), oddsChange.awayOdds());
    }
}
