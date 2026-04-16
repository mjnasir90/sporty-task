package com.sporty.feed.infrastructure.web.dto.beta;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class BetaOddsDeserializer extends StdDeserializer<BetaOdds> {

    private static final Set<String> ALLOWED_KEYS = Set.of("home", "draw", "away");

    public BetaOddsDeserializer() {
        super(BetaOdds.class);
    }

    @Override
    public BetaOdds deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (!ALLOWED_KEYS.contains(key)) {
                throw UnrecognizedPropertyException.from(p, BetaOdds.class, key, List.copyOf(ALLOWED_KEYS));
            }
        }
        Double home = node.hasNonNull("home") ? node.get("home").doubleValue() : null;
        Double draw = node.hasNonNull("draw") ? node.get("draw").doubleValue() : null;
        Double away = node.hasNonNull("away") ? node.get("away").doubleValue() : null;
        return new BetaOdds(home, draw, away);
    }
}