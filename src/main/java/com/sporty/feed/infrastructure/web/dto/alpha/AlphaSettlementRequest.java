package com.sporty.feed.infrastructure.web.dto.alpha;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

/**
 * ProviderAlpha settlement payload.
 * {@code outcome} is one of {@code "1"} (home), {@code "X"} (draw), {@code "2"} (away).
 */
@Getter
public final class AlphaSettlementRequest extends AlphaFeedRequest {

    @Schema(description = "Match outcome: 1=home win, X=draw, 2=away win", allowableValues = {"1", "X", "2"}, example = "1")
    @NotBlank(message = "outcome must not be blank")
    @Pattern(regexp = "1|X|2", message = "outcome must be one of: 1, X, 2")
    private String outcome;
}
