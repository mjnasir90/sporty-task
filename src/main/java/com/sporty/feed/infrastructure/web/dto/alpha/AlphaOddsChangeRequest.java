package com.sporty.feed.infrastructure.web.dto.alpha;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

/**
 * ProviderAlpha odds-change payload.
 * {@code values} is a typed object mapping 1X2 keys to decimal odds.
 */
@Getter
public final class AlphaOddsChangeRequest extends AlphaFeedRequest {

    @NotNull(message = "values must not be null")
    @Valid
    private AlphaOdds values;
}
