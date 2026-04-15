package com.sporty.feed.infrastructure.web.mapper;

import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.application.usecase.command.FeedCommand;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.domain.model.Outcome;
import com.sporty.feed.infrastructure.web.dto.alpha.AlphaFeedRequest;
import com.sporty.feed.infrastructure.web.dto.alpha.AlphaOdds;
import com.sporty.feed.infrastructure.web.dto.alpha.AlphaOddsChangeRequest;
import com.sporty.feed.infrastructure.web.dto.alpha.AlphaSettlementRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Infrastructure mapper: translates ProviderAlpha DTOs into application-layer commands.
 * The sealed + pattern-matching switch is exhaustive — the compiler enforces that
 * every permitted subtype is handled.
 */
@Component
public class AlphaFeedMapper {

    public FeedCommand toCommand(AlphaFeedRequest request) {
        return switch (request) {
            case AlphaOddsChangeRequest r -> {
                AlphaOdds odds = r.getValues();
                yield new OddsChangeCommand(r.getEventId(), Instant.now(),
                        odds.home(), odds.draw(), odds.away());
            }
            case AlphaSettlementRequest r -> new BetSettlementCommand(
                    r.getEventId(),
                    Instant.now(),
                    Outcome.fromAlpha(r.getOutcome())
            );
        };
    }
}
