package com.sporty.feed.infrastructure.web.dto.beta;

import jakarta.validation.constraints.Positive;

/**
 * Nested odds object in ProviderBeta ODDS messages.
 * Jackson 2.12+ deserializes records natively via the canonical constructor.
 */
public record BetaOdds(
        @Positive(message = "home odds must be positive") double home,
        @Positive(message = "draw odds must be positive") double draw,
        @Positive(message = "away odds must be positive") double away
) {}
