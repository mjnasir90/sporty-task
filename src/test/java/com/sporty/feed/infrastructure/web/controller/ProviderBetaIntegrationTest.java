package com.sporty.feed.infrastructure.web.controller;

import com.sporty.feed.domain.event.BetSettledEvent;
import com.sporty.feed.domain.event.DomainEvent;
import com.sporty.feed.domain.event.OddsChangedEvent;
import com.sporty.feed.domain.model.Outcome;
import com.sporty.feed.infrastructure.messaging.LoggingDomainEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for ProviderBeta feed endpoint.
 *
 * Loads the full context and verifies the complete chain:
 * HTTP → controller → mapper → service → domain event publisher.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProviderBetaIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoSpyBean
    LoggingDomainEventPublisher domainEventPublisher;

    // ── ODDS_CHANGE ──────────────────────────────────────────────────────────

    @Test
    void oddsChange_fullChain_publishesOddsChangedEvent() throws Exception {
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

        var captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(OddsChangedEvent.class);
        OddsChangedEvent event = (OddsChangedEvent) captor.getValue();
        assertThat(event.eventId()).isEqualTo("ev456");
        assertThat(event.homeOdds()).isEqualTo(1.95);
        assertThat(event.drawOdds()).isEqualTo(3.2);
        assertThat(event.awayOdds()).isEqualTo(4.0);
        assertThat(event.timestamp()).isNotNull();
    }

    // ── BET_SETTLEMENT ───────────────────────────────────────────────────────

    @Test
    void settlement_home_publishesBetSettledEventWithHomeOutcome() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "SETTLEMENT", "event_id": "ev456", "result": "home"}
                                """))
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(BetSettledEvent.class);
        assertThat(((BetSettledEvent) captor.getValue()).outcome()).isEqualTo(Outcome.HOME);
    }

    @Test
    void settlement_draw_publishesBetSettledEventWithDrawOutcome() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "SETTLEMENT", "event_id": "ev456", "result": "draw"}
                                """))
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());

        assertThat(((BetSettledEvent) captor.getValue()).outcome()).isEqualTo(Outcome.DRAW);
    }

    @Test
    void settlement_away_publishesBetSettledEventWithAwayOutcome() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "SETTLEMENT", "event_id": "ev456", "result": "away"}
                                """))
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());

        assertThat(((BetSettledEvent) captor.getValue()).outcome()).isEqualTo(Outcome.AWAY);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void unknownType_returns400() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "UNKNOWN", "event_id": "ev456"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void missingEventId_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "ODDS", "odds": {"home": 1.95, "draw": 3.2, "away": 4.0}}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void missingOdds_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "ODDS", "event_id": "ev456"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativeOdds_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ODDS",
                                  "event_id": "ev456",
                                  "odds": {"home": -1.0, "draw": 3.2, "away": 4.0}
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidResult_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"type": "SETTLEMENT", "event_id": "ev456", "result": "invalid"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativeOdds_returns400WithCorrectMessage() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ODDS",
                                  "event_id": "ev456",
                                  "odds": {"home": -1.0, "draw": 3.2, "away": 4.0}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("odds.home: home odds must be positive"));
    }

    @Test
    void missingOddsKey_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ODDS",
                                  "event_id": "ev456",
                                  "odds": {"home": 1.95, "draw": 3.2}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("odds.away: away odds must not be null"));
    }

    @Test
    void unknownOddsKey_returns400WithFieldName() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ODDS",
                                  "event_id": "ev456",
                                  "odds": {"home": 1.95, "draw": 3.2, "away": 4.0, "extra": 5.0}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown field 'extra' is not allowed"));
    }

    @Test
    void integerEventId_returns400() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ODDS",
                                  "event_id": 123,
                                  "odds": {"home": 1.95, "draw": 3.2, "away": 4.0}
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void blankEventId_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ODDS",
                                  "event_id": "",
                                  "odds": {"home": 1.95, "draw": 3.2, "away": 4.0}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("eventId: event_id must not be blank"));
    }

    @Test
    void whitespaceEventId_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-beta/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "type": "ODDS",
                                  "event_id": "   ",
                                  "odds": {"home": 1.95, "draw": 3.2, "away": 4.0}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("eventId: event_id must not be blank"));
    }
}
