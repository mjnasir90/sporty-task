package com.sporty.feed.infrastructure.web.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.domain.model.Outcome;
import com.sporty.feed.infrastructure.web.dto.alpha.AlphaFeedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AlphaFeedMapperTest {

    private AlphaFeedMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mapper = new AlphaFeedMapper();
        objectMapper = new ObjectMapper();
    }

    @Test
    void mapsOddsUpdateToOddsChangeCommand() throws Exception {
        var json = """
                {
                  "msg_type": "odds_update",
                  "event_id": "ev123",
                  "values": {"1": 2.0, "X": 3.1, "2": 3.8}
                }
                """;

        var result = mapper.toCommand(objectMapper.readValue(json, AlphaFeedRequest.class));

        assertThat(result).isInstanceOf(OddsChangeCommand.class);
        var cmd = (OddsChangeCommand) result;
        assertThat(cmd.eventId()).isEqualTo("ev123");
        assertThat(cmd.homeOdds()).isEqualTo(2.0);
        assertThat(cmd.drawOdds()).isEqualTo(3.1);
        assertThat(cmd.awayOdds()).isEqualTo(3.8);
    }

    @Test
    void mapsSettlementToBetSettlementCommand() throws Exception {
        var json = """
                {"msg_type": "settlement", "event_id": "ev123", "outcome": "X"}
                """;

        var result = mapper.toCommand(objectMapper.readValue(json, AlphaFeedRequest.class));

        assertThat(result).isInstanceOf(BetSettlementCommand.class);
        assertThat(((BetSettlementCommand) result).outcome()).isEqualTo(Outcome.DRAW);
    }

    @Test
    void mapsAllAlphaOutcomes() throws Exception {
        assertOutcome("1", Outcome.HOME);
        assertOutcome("X", Outcome.DRAW);
        assertOutcome("2", Outcome.AWAY);
    }

    private void assertOutcome(String value, Outcome expected) throws Exception {
        var json = """
                {"msg_type":"settlement","event_id":"ev1","outcome":"%s"}
                """.formatted(value);
        var result = (BetSettlementCommand) mapper.toCommand(
                objectMapper.readValue(json, AlphaFeedRequest.class));
        assertThat(result.outcome()).isEqualTo(expected);
    }
}
