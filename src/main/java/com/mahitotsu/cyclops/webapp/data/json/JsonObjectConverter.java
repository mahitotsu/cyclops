package com.mahitotsu.cyclops.webapp.data.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class JsonObjectConverter implements AttributeConverter<Object, JsonNode> {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public JsonNode convertToDatabaseColumn(final Object attribute) {
        final JsonNode jsonNode = this.objectMapper.valueToTree(attribute);
        ObjectNode.class.cast(jsonNode).put("_class", attribute.getClass().getName());
        return jsonNode;
    }

    @Override
    public Object convertToEntityAttribute(final JsonNode dbData) {
        try {
            final String javaTypeName = dbData.get("_class").asText();
            final Class<?> javaType = Class.forName(javaTypeName);
            ObjectNode.class.cast(dbData).remove("_class");
            return this.objectMapper.treeToValue(dbData, javaType);
        } catch (final ClassNotFoundException | JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}