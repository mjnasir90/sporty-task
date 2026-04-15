package com.sporty.feed.domain.event;

import com.sporty.feed.domain.model.Outcome;

import java.time.Instant;

public record BetSettledEvent(
        String eventId,
        Instant timestamp,
        Outcome outcome
) implements DomainEvent {}
