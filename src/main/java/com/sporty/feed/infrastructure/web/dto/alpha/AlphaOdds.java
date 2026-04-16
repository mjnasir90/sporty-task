package com.sporty.feed.infrastructure.web.dto.alpha;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

/**
 * Typed odds object for ProviderAlpha.
 * Maps 1X2 JSON keys to readable Java names via {@link JsonProperty}.
 * Replaces the previous {@code Map<String, Double>} to enforce exactly the three required fields.
 */
@JsonDeserialize(using = AlphaOddsDeserializer.class)
public record AlphaOdds(
        @JsonProperty("1") @NotNull(message = "home odds(1) must not be null") @Positive(message = "home odds(1) must be positive")
        @Schema(description = "Home win odds", example = "2.10") Double home,

        @JsonProperty("X") @NotNull(message = "draw odds(X) must not be null") @Positive(message = "draw odds(X) must be positive")
        @Schema(description = "Draw odds", example = "3.20") Double draw,

        @JsonProperty("2") @NotNull(message = "away odds(2) must not be null") @Positive(message = "away odds(2) must be positive")
        @Schema(description = "Away win odds", example = "3.50") Double away
) {}
