package com.sporty.feed.infrastructure.web.dto.beta;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Sealed base for ProviderBeta feed messages.
 * Discriminator field is {@code type}: {@code "ODDS"} or {@code "SETTLEMENT"}.
 */
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = BetaOddsChangeRequest.class, name = "ODDS"),
        @JsonSubTypes.Type(value = BetaSettlementRequest.class, name = "SETTLEMENT")
})
public sealed abstract class BetaFeedRequest
        permits BetaOddsChangeRequest, BetaSettlementRequest {

    @Schema(description = "Unique event identifier", example = "match-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "event_id must not be blank")
    @JsonProperty("event_id")
    private String eventId;
}
