package com.sporty.feed.infrastructure.web.dto.alpha;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

/**
 * Sealed base for ProviderAlpha feed messages.
 *
 * Jackson reads {@code msg_type} and deserializes directly into the correct subtype —
 * no manual type-switching at the controller level.
 *
 * {@code @JsonAutoDetect} lets Jackson populate the private {@code eventId} field
 * without a public setter on the base class.
 */
@Getter
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "msg_type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AlphaOddsChangeRequest.class, name = "odds_update"),
        @JsonSubTypes.Type(value = AlphaSettlementRequest.class, name = "settlement")
})
public sealed abstract class AlphaFeedRequest
        permits AlphaOddsChangeRequest, AlphaSettlementRequest {

    @Schema(description = "Unique event identifier", example = "match-001", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "event_id must not be blank")
    @JsonProperty("event_id")
    private String eventId;
}
