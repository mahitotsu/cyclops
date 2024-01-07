package com.mahitotsu.cyclops.webapp.data.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonValueConverter implements AttributeConverter<JsonValue<?>, JsonNode> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JsonNode convertToDatabaseColumn(final JsonValue<?> attribute) {
        return this.objectMapper.valueToTree(attribute);
    }

    @Override
    public JsonValue<?> convertToEntityAttribute(final JsonNode dbData) {
        try {
            final String javaTypeName = dbData.get("javaType").asText();
            final Class<?> javaType = Class.forName(javaTypeName);
            return javaType.asSubclass(JsonValue.class).cast(this.objectMapper.treeToValue(dbData, javaType));
        } catch (final ClassNotFoundException | JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}