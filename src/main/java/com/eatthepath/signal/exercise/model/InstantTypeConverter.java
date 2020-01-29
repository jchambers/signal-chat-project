package com.eatthepath.signal.exercise.model;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.Instant;

/**
 * A type converter that allows GSON to serialize and deserialize {@link Instant} instances as {@code long} values.
 */
public class InstantTypeConverter implements JsonSerializer<Instant>, JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(final JsonElement jsonElement, final Type type, final JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        if (jsonElement.isJsonNull()) {
            return null;
        } else if (jsonElement.isJsonPrimitive()) {
            return Instant.ofEpochMilli(jsonElement.getAsLong());
        } else {
            throw new JsonParseException("Could not parse non-primitive as Instant");
        }
    }

    @Override
    public JsonElement serialize(final Instant instant, final Type type, final JsonSerializationContext jsonSerializationContext) {
        return new JsonPrimitive(instant.toEpochMilli());
    }
}
