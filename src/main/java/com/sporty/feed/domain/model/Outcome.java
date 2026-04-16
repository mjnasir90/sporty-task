package com.sporty.feed.domain.model;

public enum Outcome {
    HOME, DRAW, AWAY;

    /** Alpha encodes outcome as "1" (home), "X" (draw), "2" (away). */
    public static Outcome fromAlpha(String value) {
        return switch (value) {
            case "1" -> HOME;
            case "X" -> DRAW;
            case "2" -> AWAY;
            default  -> throw new UnknownOutcomeException("Unknown Alpha outcome: " + value);
        };
    }

    /** Beta encodes outcome as "home", "draw", "away" (exact case, per spec). */
    public static Outcome fromBeta(String value) {
        return switch (value) {
            case "home" -> HOME;
            case "draw" -> DRAW;
            case "away" -> AWAY;
            default     -> throw new UnknownOutcomeException("Unknown Beta outcome: " + value);
        };
    }
}
