package com.sporty.feed.infrastructure.web.controller;

import com.sporty.feed.application.usecase.BetSettlementUseCase;
import com.sporty.feed.application.usecase.OddsChangeUseCase;
import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.domain.model.Outcome;
import com.sporty.feed.infrastructure.web.advice.GlobalExceptionHandler;
import com.sporty.feed.infrastructure.web.mapper.BetaFeedMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderBetaController.class)
@Import({BetaFeedMapper.class, GlobalExceptionHandler.class})
class ProviderBetaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OddsChangeUseCase oddsChangeUseCase;

    @MockitoBean
    BetSettlementUseCase betSettlementUseCase;

    @Test
    void odds_routesToOddsChangeUseCase() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ODDS",
                                  "event_id": "ev456",
                                  "odds": {"home": 1.95, "draw": 3.2, "away": 4.0}
                                }
                                """))
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(OddsChangeCommand.class);
        verify(oddsChangeUseCase).execute(captor.capture());
        verifyNoInteractions(betSettlementUseCase);

        assertThat(captor.getValue().eventId()).isEqualTo("ev456");
        assertThat(captor.getValue().homeOdds()).isEqualTo(1.95);
        assertThat(captor.getValue().drawOdds()).isEqualTo(3.2);
        assertThat(captor.getValue().awayOdds()).isEqualTo(4.0);
    }

    @Test
    void settlement_routesToBetSettlementUseCase() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "SETTLEMENT",
                                  "event_id": "ev456",
                                  "result": "away"
                                }
                                """))
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(BetSettlementCommand.class);
        verify(betSettlementUseCase).execute(captor.capture());
        verifyNoInteractions(oddsChangeUseCase);

        assertThat(captor.getValue().outcome()).isEqualTo(Outcome.AWAY);
    }

    @Test
    void unknownType_returns400() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "UNKNOWN", "event_id": "ev1"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
