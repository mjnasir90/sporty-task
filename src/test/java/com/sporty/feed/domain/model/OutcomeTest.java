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
    void fromAlpha_unknownValue_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Outcome.fromAlpha("Z"))
                .isInstanceOf(IllegalArgumentException.class)
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
    void fromBeta_unknownValue_throwsIllegalArgumentException() {
        assertThatThrownBy(() -> Outcome.fromBeta("invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("invalid");
    }
}
