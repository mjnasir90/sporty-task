package com.sporty.feed.domain.model;

import java.time.Instant;

/**
 * Normalized bet-settlement event. Outcome is one of {@link Outcome#HOME}, {@link Outcome#DRAW},
 * or {@link Outcome#AWAY} regardless of the originating provider's encoding.
 */
public record BetSettlementMessage(
        String eventId,
        Instant timestamp,
        Outcome outcome
) implements FeedMessage {}
