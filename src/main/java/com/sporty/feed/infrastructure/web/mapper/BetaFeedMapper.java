package com.sporty.feed.infrastructure.web.mapper;

import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.application.usecase.command.BettingCommand;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.domain.model.Outcome;
import com.sporty.feed.infrastructure.web.dto.beta.BetaFeedRequest;
import com.sporty.feed.infrastructure.web.dto.beta.BetaOddsChangeRequest;
import com.sporty.feed.infrastructure.web.dto.beta.BetaSettlementRequest;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * Infrastructure mapper: translates ProviderBeta DTOs into application-layer commands.
 */
@Component
public class BetaFeedMapper {

    public BettingCommand toCommand(BetaFeedRequest request) {
        return switch (request) {
            case BetaOddsChangeRequest r -> new OddsChangeCommand(
                    r.getEventId(),
                    Instant.now(),
                    r.getOdds().home(),
                    r.getOdds().draw(),
                    r.getOdds().away()
            );
            case BetaSettlementRequest r -> new BetSettlementCommand(
                    r.getEventId(),
                    Instant.now(),
                    Outcome.fromBeta(r.getResult())
            );
        };
    }
}
