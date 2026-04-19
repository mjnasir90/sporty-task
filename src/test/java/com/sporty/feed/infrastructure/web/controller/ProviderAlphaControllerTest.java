package com.sporty.feed.infrastructure.web.controller;

import com.sporty.feed.application.usecase.BetSettlementUseCase;
import com.sporty.feed.application.usecase.OddsChangeUseCase;
import com.sporty.feed.application.usecase.command.BetSettlementCommand;
import com.sporty.feed.application.usecase.command.OddsChangeCommand;
import com.sporty.feed.infrastructure.web.advice.GlobalExceptionHandler;
import com.sporty.feed.infrastructure.web.mapper.AlphaFeedMapper;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderAlphaController.class)
@Import({AlphaFeedMapper.class, GlobalExceptionHandler.class})
class ProviderAlphaControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OddsChangeUseCase oddsChangeUseCase;

    @MockitoBean
    BetSettlementUseCase betSettlementUseCase;

    @Test
    void oddsUpdate_routesToOddsChangeUseCase() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "event_id": "ev123",
                                  "values": {"1": 2.0, "X": 3.1, "2": 3.8}
                                }
                                """))
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(OddsChangeCommand.class);
        verify(oddsChangeUseCase).execute(captor.capture());
        verifyNoInteractions(betSettlementUseCase);

        assertThat(captor.getValue().eventId()).isEqualTo("ev123");
        assertThat(captor.getValue().homeOdds()).isEqualTo(2.0);
        assertThat(captor.getValue().drawOdds()).isEqualTo(3.1);
        assertThat(captor.getValue().awayOdds()).isEqualTo(3.8);
    }

    @Test
    void settlement_routesToBetSettlementUseCase() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "settlement", "event_id": "ev123", "outcome": "1"}
                                """))
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(BetSettlementCommand.class);
        verify(betSettlementUseCase).execute(captor.capture());
        verifyNoInteractions(oddsChangeUseCase);

        assertThat(captor.getValue().eventId()).isEqualTo("ev123");
    }

    @Test
    void unknownMsgType_returns400() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "unknown_type", "event_id": "ev1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown type 'unknown_type'. Valid values: [odds_update, settlement]"));
    }
}
