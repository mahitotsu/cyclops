package com.mahitotsu.cyclops.webapp;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
public class Main {
    public static void main(final String... args) {
        new SpringApplicationBuilder(Main.class).run(args);
    }

    @Bean(name = "conversionService")
    public ConversionServiceFactoryBean conversionServiceFactoryBean() {

        final ObjectMapper mapper = new ObjectMapper();

        final ConversionServiceFactoryBean fb = new ConversionServiceFactoryBean();
        fb.setConverters(Stream.of(
                new ObjectToJsonNodeConverter(mapper),
                new JsonNodeToObjectConverter(mapper))
                .collect(Collectors.toSet()));
        return fb;
    }

    private static class ObjectToJsonNodeConverter implements Converter<Object, JsonNode> {

        private ObjectToJsonNodeConverter(final ObjectMapper mapper) {
            this.mapper = mapper;
        }

        private final ObjectMapper mapper;

        @Override
        @Nullable
        public JsonNode convert(final Object source) {
            return this.mapper.valueToTree(source);
        }
    }

    private static class JsonNodeToObjectConverter implements ConverterFactory<JsonNode, Object> {

        private JsonNodeToObjectConverter(final ObjectMapper mapper) {
            this.mapper = mapper;
        }

        private final ObjectMapper mapper;

        @Override
        public <T> Converter<JsonNode, T> getConverter(Class<T> targetType) {
            return new Converter<JsonNode, T>() {
                @Override
                @Nullable
                public T convert(final JsonNode source) {
                    try {
                        return JsonNodeToObjectConverter.this.mapper.treeToValue(source, targetType);
                    } catch (JsonProcessingException e) {
                        throw new IllegalStateException(e);
                    }
                }
            };
        }
    }
}