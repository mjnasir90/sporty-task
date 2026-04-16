package com.sporty.feed.domain.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutcomeTest {

    // ── fromAlpha ────────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "1, HOME",
            "X, DRAW",
            "2, AWAY"
    })
    void fromAlpha_mapsAllValidValues(String input, Outcome expected) {
        assertThat(Outcome.fromAlpha(input)).isEqualTo(expected);
    }

    @Test
    void fromAlpha_unknownValue_throwsUnknownOutcomeException() {
        assertThatThrownBy(() -> Outcome.fromAlpha("Z"))
                .isInstanceOf(UnknownOutcomeException.class)
                .hasMessageContaining("Z");
    }

    // ── fromBeta ─────────────────────────────────────────────────────────────

    @ParameterizedTest
    @CsvSource({
            "home, HOME",
            "draw, DRAW",
            "away, AWAY"
    })
    void fromBeta_mapsAllValidValues(String input, Outcome expected) {
        assertThat(Outcome.fromBeta(input)).isEqualTo(expected);
    }

    @Test
    void fromBeta_unknownValue_throwsUnknownOutcomeException() {
        assertThatThrownBy(() -> Outcome.fromBeta("invalid"))
                .isInstanceOf(UnknownOutcomeException.class)
                .hasMessageContaining("invalid");
    }

    @Test
    void fromBeta_uppercaseValue_throwsUnknownOutcomeException() {
        // Beta spec requires exact lowercase — "HOME", "DRAW", "AWAY" are not valid
        assertThatThrownBy(() -> Outcome.fromBeta("HOME"))
                .isInstanceOf(UnknownOutcomeException.class);
        assertThatThrownBy(() -> Outcome.fromBeta("DRAW"))
                .isInstanceOf(UnknownOutcomeException.class);
        assertThatThrownBy(() -> Outcome.fromBeta("AWAY"))
                .isInstanceOf(UnknownOutcomeException.class);
    }
}
