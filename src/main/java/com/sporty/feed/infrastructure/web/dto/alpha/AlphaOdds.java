package com.sporty.feed.infrastructure.web.dto.alpha;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Positive;

/**
 * Typed odds object for ProviderAlpha.
 * Maps 1X2 JSON keys to readable Java names via {@link JsonProperty}.
 * Replaces the previous {@code Map<String, Double>} to enforce exactly the three required fields.
 */
public record AlphaOdds(
        @JsonProperty("1") @Positive(message = "home odds must be positive") double home,
        @JsonProperty("X") @Positive(message = "draw odds must be positive") double draw,
        @JsonProperty("2") @Positive(message = "away odds must be positive") double away
) {}
