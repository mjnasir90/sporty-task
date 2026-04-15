package com.sporty.feed.domain.event;

import java.time.Instant;

public record OddsChangedEvent(
        String eventId,
        Instant timestamp,
        double homeOdds,
        double drawOdds,
        double awayOdds
) implements DomainEvent {}
