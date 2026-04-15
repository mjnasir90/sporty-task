package com.sporty.feed.domain.model;

import java.time.Instant;

/**
 * Sealed interface representing a normalized message from any feed provider.
 * Permitted subtypes cover all known message types in the sports betting feed pipeline.
 */
public sealed interface FeedMessage
        permits OddsChangeMessage, BetSettlementMessage {

    String eventId();
    Instant timestamp();
}
