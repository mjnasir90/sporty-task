package com.sporty.feed.infrastructure.web.controller;

import com.sporty.feed.application.usecase.BetSettlementUseCase;
import com.sporty.feed.application.usecase.OddsChangeUseCase;
import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.infrastructure.web.dto.beta.BetaFeedRequest;
import com.sporty.feed.infrastructure.web.mapper.BetaFeedMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/provider-beta")
@RequiredArgsConstructor
@Tag(name = "Provider Beta", description = "Feed ingestion endpoint for Provider Beta (type discriminator)")
public class ProviderBetaController {

    private final BetaFeedMapper mapper;
    private final OddsChangeUseCase oddsChangeUseCase;
    private final BetSettlementUseCase betSettlementUseCase;

    @PostMapping("/feed")
    @Operation(
            summary = "Ingest a feed message from Provider Beta",
            description = """
                    Accepts a polymorphic JSON message discriminated by `type`.

                    **Supported types:**
                    - `ODDS` — odds change for a 1X2 market
                    - `SETTLEMENT` — final outcome of an event
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Message accepted for processing"),
            @ApiResponse(responseCode = "400", description = "Unknown type, missing fields, or invalid values",
                    content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(example = "{\"message\": \"Validation failed\", \"errors\": [\"eventId: event_id must not be blank\"]}"))),
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    examples = {
                            @ExampleObject(name = "Odds", value = """
                                    {
                                      "type": "ODDS",
                                      "event_id": "match-001",
                                      "odds": { "home": 1.95, "draw": 3.20, "away": 4.00 }
                                    }
                                    """),
                            @ExampleObject(name = "Settlement", value = """
                                    {
                                      "type": "SETTLEMENT",
                                      "event_id": "match-001",
                                      "result": "home"
                                    }
                                    """)
                    }
            )
    )
    public ResponseEntity<Void> handleFeed(@RequestBody @Valid BetaFeedRequest request) {
        switch (mapper.toCommand(request)) {
            case OddsChangeCommand cmd    -> oddsChangeUseCase.execute(cmd);
            case BetSettlementCommand cmd -> betSettlementUseCase.execute(cmd);
        }
        return ResponseEntity.accepted().build();
    }
}