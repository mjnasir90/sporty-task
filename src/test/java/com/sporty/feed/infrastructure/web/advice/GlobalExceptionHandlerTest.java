package com.sporty.feed.infrastructure.web.advice;

import com.sporty.feed.application.usecase.BetSettlementUseCase;
import com.sporty.feed.application.usecase.OddsChangeUseCase;
import com.sporty.feed.infrastructure.web.controller.ProviderAlphaController;
import com.sporty.feed.infrastructure.web.mapper.AlphaFeedMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProviderAlphaController.class)
@Import({AlphaFeedMapper.class, GlobalExceptionHandler.class})
class GlobalExceptionHandlerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    OddsChangeUseCase oddsChangeUseCase;

    @MockitoBean
    BetSettlementUseCase betSettlementUseCase;

    // ── HttpMessageNotReadableException ──────────────────────────────────────

    @Test
    void unknownMsgType_returns400_withMessage() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "unknown", "event_id": "ev1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown type 'unknown'. Valid values: [odds_update, settlement]"));
    }

    @Test
    void malformedJson_returns400_withMessage() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not valid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unrecognized or malformed message format"));
    }

    // ── MethodArgumentNotValidException ──────────────────────────────────────

    @Test
    void missingEventId_returns400_withValidationErrors() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "values": {"1": 2.0, "X": 3.1, "2": 3.8}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0]").exists());
    }

    @Test
    void missingValues_returns400_withValidationErrors() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "odds_update", "event_id": "ev1"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void invalidOutcome_returns400_withValidationErrors() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "settlement", "event_id": "ev1", "outcome": "Z"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    void emptyBody_returns400() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }
}
