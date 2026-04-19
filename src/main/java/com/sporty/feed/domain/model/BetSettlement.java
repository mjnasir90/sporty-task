package com.sporty.feed.domain.model;

import java.time.Instant;

/**
 * Domain object representing a normalized bet settlement.
 * Outcome is one of {@link Outcome#HOME}, {@link Outcome#DRAW}, or {@link Outcome#AWAY}
 * regardless of the originating provider's encoding.
 */
public record BetSettlement(
        String eventId,
        Instant timestamp,
        Outcome outcome
) {}