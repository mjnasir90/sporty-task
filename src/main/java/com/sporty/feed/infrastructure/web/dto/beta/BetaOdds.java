package com.sporty.feed.infrastructure.web.dto.beta;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Nested odds object in ProviderBeta ODDS messages.
 * Jackson 2.12+ deserializes records natively via the canonical constructor.
 */
@JsonDeserialize(using = BetaOddsDeserializer.class)
public record BetaOdds(
        @NotNull(message = "home odds must not be null") @Positive(message = "home odds must be positive")
        @Schema(description = "Home win odds", example = "1.95") Double home,

        @NotNull(message = "draw odds must not be null") @Positive(message = "draw odds must be positive")
        @Schema(description = "Draw odds", example = "3.20") Double draw,

        @NotNull(message = "away odds must not be null") @Positive(message = "away odds must be positive")
        @Schema(description = "Away win odds", example = "4.00") Double away
) {}
