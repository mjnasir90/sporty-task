package com.sporty.feed.infrastructure.web.dto.alpha;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

/**
 * ProviderAlpha settlement payload.
 * {@code outcome} is one of {@code "1"} (home), {@code "X"} (draw), {@code "2"} (away).
 */
@Getter
public final class AlphaSettlementRequest extends AlphaFeedRequest {

    @NotBlank(message = "outcome must not be blank")
    @Pattern(regexp = "1|X|2", message = "outcome must be one of: 1, X, 2")
    private String outcome;
}
