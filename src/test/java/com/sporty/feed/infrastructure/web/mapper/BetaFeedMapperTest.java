package com.sporty.feed.infrastructure.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.domain.model.Outcome;
import com.sporty.feed.infrastructure.web.dto.beta.BetaFeedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BetaFeedMapperTest {

    private BetaFeedMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mapper = new BetaFeedMapper();
        objectMapper = new ObjectMapper();
    }

    @Test
    void mapsOddsToOddsChangeCommand() throws Exception {
        var json = """
                {
                  "type": "ODDS",
                  "event_id": "ev456",
                  "odds": {"home": 1.95, "draw": 3.2, "away": 4.0}
                }
                """;

        var result = mapper.toCommand(objectMapper.readValue(json, BetaFeedRequest.class));

        assertThat(result).isInstanceOf(OddsChangeCommand.class);
        var cmd = (OddsChangeCommand) result;
        assertThat(cmd.eventId()).isEqualTo("ev456");
        assertThat(cmd.homeOdds()).isEqualTo(1.95);
        assertThat(cmd.drawOdds()).isEqualTo(3.2);
        assertThat(cmd.awayOdds()).isEqualTo(4.0);
    }

    @Test
    void mapsSettlementToBetSettlementCommand() throws Exception {
        var json = """
                {"type": "SETTLEMENT", "event_id": "ev456", "result": "away"}
                """;

        var result = mapper.toCommand(objectMapper.readValue(json, BetaFeedRequest.class));

        assertThat(result).isInstanceOf(BetSettlementCommand.class);
        assertThat(((BetSettlementCommand) result).outcome()).isEqualTo(Outcome.AWAY);
    }

    @Test
    void mapsAllBetaOutcomes() throws Exception {
        assertOutcome("home", Outcome.HOME);
        assertOutcome("draw", Outcome.DRAW);
        assertOutcome("away", Outcome.AWAY);
    }

    private void assertOutcome(String value, Outcome expected) throws Exception {
        var json = """
                {"type":"SETTLEMENT","event_id":"ev1","result":"%s"}
                """.formatted(value);
        var result = (BetSettlementCommand) mapper.toCommand(
                objectMapper.readValue(json, BetaFeedRequest.class));
        assertThat(result.outcome()).isEqualTo(expected);
    }
}
