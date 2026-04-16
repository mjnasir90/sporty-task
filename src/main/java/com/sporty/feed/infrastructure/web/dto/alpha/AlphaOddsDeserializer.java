package com.sporty.feed.infrastructure.web.dto.alpha;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class AlphaOddsDeserializer extends StdDeserializer<AlphaOdds> {

    private static final Set<String> ALLOWED_KEYS = Set.of("1", "X", "2");

    public AlphaOddsDeserializer() {
        super(AlphaOdds.class);
    }

    @Override
    public AlphaOdds deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode node = p.getCodec().readTree(p);
        Iterator<String> fieldNames = node.fieldNames();
        while (fieldNames.hasNext()) {
            String key = fieldNames.next();
            if (!ALLOWED_KEYS.contains(key)) {
                throw UnrecognizedPropertyException.from(p, AlphaOdds.class, key, List.copyOf(ALLOWED_KEYS));
            }
        }
        Double home = node.hasNonNull("1") ? node.get("1").doubleValue() : null;
        Double draw = node.hasNonNull("X") ? node.get("X").doubleValue() : null;
        Double away = node.hasNonNull("2") ? node.get("2").doubleValue() : null;
        return new AlphaOdds(home, draw, away);
    }
}
