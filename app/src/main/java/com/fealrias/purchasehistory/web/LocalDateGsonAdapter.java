package com.fealrias.purchasehistory.web;


import com.google.gson.*;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateGsonAdapter implements JsonSerializer<LocalDateTime>, JsonDeserializer<LocalDateTime> {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public JsonElement serialize(LocalDateTime localDateTime,
                                 Type type,
                                 JsonSerializationContext jsonSerializationContext) {

        return new JsonPrimitive(localDateTime.format(formatter));

    }

    @Override
    public LocalDateTime deserialize(JsonElement jsonElement,
                                     Type type,
                                     JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {

        return LocalDateTime.parse(jsonElement.getAsJsonPrimitive().getAsString(), formatter);
    }

}