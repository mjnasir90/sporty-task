package com.sporty.feed.infrastructure.web.dto.beta;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

/**
 * ProviderBeta settlement payload.
 * {@code result} is one of {@code "home"}, {@code "draw"}, {@code "away"}.
 */
@Getter
public final class BetaSettlementRequest extends BetaFeedRequest {

    @NotBlank(message = "result must not be blank")
    @Pattern(regexp = "home|draw|away", message = "result must be one of: home, draw, away")
    private String result;
}
