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
 * Integration tests for ProviderAlpha feed endpoint.
 *
 * Uses {@code @SpringBootTest} to load the full application context, verifying the
 * complete chain: HTTP → controller → mapper → service → domain event publisher.
 *
 * Contrast with {@link ProviderAlphaControllerTest} which is a web-layer slice test
 * that mocks the use case and only verifies controller + mapper behaviour.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ProviderAlphaIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoSpyBean
    LoggingDomainEventPublisher domainEventPublisher;

    // ── ODDS_CHANGE ──────────────────────────────────────────────────────────

    @Test
    void oddsChange_fullChain_publishesOddsChangedEvent() throws Exception {
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

        var captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(OddsChangedEvent.class);
        OddsChangedEvent event = (OddsChangedEvent) captor.getValue();
        assertThat(event.eventId()).isEqualTo("ev123");
        assertThat(event.homeOdds()).isEqualTo(2.0);
        assertThat(event.drawOdds()).isEqualTo(3.1);
        assertThat(event.awayOdds()).isEqualTo(3.8);
        assertThat(event.timestamp()).isNotNull();
    }

    // ── BET_SETTLEMENT ───────────────────────────────────────────────────────

    @Test
    void settlement_home_publishesBetSettledEventWithHomeOutcome() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "settlement", "event_id": "ev123", "outcome": "1"}
                                """))
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());

        assertThat(captor.getValue()).isInstanceOf(BetSettledEvent.class);
        assertThat(((BetSettledEvent) captor.getValue()).outcome()).isEqualTo(Outcome.HOME);
    }

    @Test
    void settlement_draw_publishesBetSettledEventWithDrawOutcome() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "settlement", "event_id": "ev123", "outcome": "X"}
                                """))
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());

        assertThat(((BetSettledEvent) captor.getValue()).outcome()).isEqualTo(Outcome.DRAW);
    }

    @Test
    void settlement_away_publishesBetSettledEventWithAwayOutcome() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "settlement", "event_id": "ev123", "outcome": "2"}
                                """))
                .andExpect(status().isAccepted());

        var captor = ArgumentCaptor.forClass(DomainEvent.class);
        verify(domainEventPublisher).publish(captor.capture());

        assertThat(((BetSettledEvent) captor.getValue()).outcome()).isEqualTo(Outcome.AWAY);
    }

    // ── Validation ───────────────────────────────────────────────────────────

    @Test
    void unknownMsgType_returns400() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "unknown", "event_id": "ev123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void missingEventId_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "odds_update", "values": {"1": 2.0, "X": 3.1, "2": 3.8}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("eventId: event_id must not be blank"));
    }

    @Test
    void missingValues_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "odds_update", "event_id": "ev123"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("values: values must not be null"));
    }

    @Test
    void invalidOutcome_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"msg_type": "settlement", "event_id": "ev123", "outcome": "Z"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.errors[0]").value("outcome: outcome must be one of: 1, X, 2"));
    }

    @Test
    void negativeOdds_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "event_id": "ev123",
                                  "values": {"1": -1.0, "X": 3.1, "2": 3.8}
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void negativeOdds_returns400WithCorrectMessage() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "event_id": "ev123",
                                  "values": {"1": -1.0, "X": 3.1, "2": 3.8}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("values.home: home odds(1) must be positive"));
    }

    @Test
    void blankEventId_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "event_id": "",
                                  "values": {"1": 2.0, "X": 3.1, "2": 3.8}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("eventId: event_id must not be blank"));
    }

    @Test
    void whitespaceEventId_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "event_id": "   ",
                                  "values": {"1": 2.0, "X": 3.1, "2": 3.8}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("eventId: event_id must not be blank"));
    }

    @Test
    void missingOddsKey_returns400WithValidationError() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "event_id": "ev123",
                                  "values": {"1": 2.0, "X": 3.1}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("values.away: away odds(2) must not be null"));
    }

    @Test
    void integerEventId_returns400() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "event_id": 123,
                                  "values": {"1": 2.0, "X": 3.1, "2": 3.8}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unrecognized or malformed message format"));
    }

    @Test
    void unknownOddsKey_returns400WithFieldName() throws Exception {
        mockMvc.perform(post("/provider-alpha/feed")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "msg_type": "odds_update",
                                  "event_id": "ev123",
                                  "values": {"1": 2.0, "X": 3.1, "2": 3.8, "3": 4.0}
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Unknown field '3' is not allowed"));
    }
}
