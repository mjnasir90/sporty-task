package com.sporty.feed.domain.event;

import com.sporty.feed.domain.model.OddsChangeMessage;

import java.time.Instant;

public record OddsChangedEvent(
        String eventId,
        Instant timestamp,
        double homeOdds,
        double drawOdds,
        double awayOdds
) implements DomainEvent {

    public static OddsChangedEvent from(OddsChangeMessage message) {
        return new OddsChangedEvent(
                message.eventId(), message.timestamp(),
                message.homeOdds(), message.drawOdds(), message.awayOdds());
    }
}
