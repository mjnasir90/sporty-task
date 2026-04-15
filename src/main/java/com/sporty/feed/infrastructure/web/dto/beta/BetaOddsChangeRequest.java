package com.sporty.feed.infrastructure.web.dto.beta;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * ProviderBeta odds-change payload.
 * Odds are nested under {@code odds} with keys {@code home}, {@code draw}, {@code away}.
 */
@Getter
public final class BetaOddsChangeRequest extends BetaFeedRequest {

    @NotNull(message = "odds must not be null")
    @Valid
    private BetaOdds odds;
}
