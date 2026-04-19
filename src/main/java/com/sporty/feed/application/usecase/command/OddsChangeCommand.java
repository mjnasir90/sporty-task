package com.sporty.feed.application.usecase.command;

import java.time.Instant;

public record OddsChangeCommand(
        String eventId,
        Instant timestamp,
        double homeOdds,
        double drawOdds,
        double awayOdds
) implements BettingCommand {}
