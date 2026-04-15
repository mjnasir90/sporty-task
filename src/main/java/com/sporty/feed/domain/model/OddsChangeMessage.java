package com.sporty.feed.domain.model;

import java.time.Instant;

/**
 * Normalized odds-change event. Odds are expressed as decimals for home / draw / away outcomes.
 */
public record OddsChangeMessage(
        String eventId,
        Instant timestamp,
        double homeOdds,
        double drawOdds,
        double awayOdds
) implements FeedMessage {}
