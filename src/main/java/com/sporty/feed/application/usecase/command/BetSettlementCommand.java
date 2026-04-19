package com.sporty.feed.application.usecase.command;

import com.sporty.feed.domain.model.Outcome;

import java.time.Instant;

public record BetSettlementCommand(
        String eventId,
        Instant timestamp,
        Outcome outcome
) implements BettingCommand {}
