package com.sporty.feed.domain.model;

import java.time.Instant;

/**
 * Domain object representing a normalized odds change.
 * Odds are expressed as decimals for home / draw / away outcomes.
 */
public record OddsChange(
        String eventId,
        Instant timestamp,
        double homeOdds,
        double drawOdds,
        double awayOdds
) {}