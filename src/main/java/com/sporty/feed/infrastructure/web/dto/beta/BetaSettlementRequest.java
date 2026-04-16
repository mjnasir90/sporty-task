package com.sporty.feed.infrastructure.web.dto.beta;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

/**
 * ProviderBeta settlement payload.
 * {@code result} is one of {@code "home"}, {@code "draw"}, {@code "away"}.
 */
@Getter
public final class BetaSettlementRequest extends BetaFeedRequest {

    @Schema(description = "Match outcome", allowableValues = {"home", "draw", "away"}, example = "home")
    @NotBlank(message = "result must not be blank")
    @Pattern(regexp = "home|draw|away", message = "result must be one of: home, draw, away")
    private String result;
}
