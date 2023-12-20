package com.mahitotsu.cyclops.webapp;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ConversionServiceFactoryBean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.lang.Nullable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Configuration
public class ConversionServiceConfiguration {

    private final ObjectMapper mapper = new ObjectMapper();

    @Bean
    public ConversionServiceFactoryBean conversionService() {

        final ConversionServiceFactoryBean fb = new ConversionServiceFactoryBean();
        fb.setConverters(Stream.of(
                this.valueToJsonConverter(),
                this.jsonToValueConverter())
                .collect(Collectors.toSet()));
        return fb;
    }

    @Bean
    public Converter<Object, JsonNode> valueToJsonConverter() {
        return new Converter<>() {
            @Override
            @Nullable
            public JsonNode convert(final Object source) {
                return source == null ? null : ConversionServiceConfiguration.this.mapper.valueToTree(source);
            }
        };
    }

    @Bean
    public ConverterFactory<JsonNode, Object> jsonToValueConverter() {
        return new ConverterFactory<JsonNode, Object>() {
            @Override
            public <T> Converter<JsonNode, T> getConverter(final Class<T> targetType) {
                return new Converter<>() {
                    @Override
                    @Nullable
                    public T convert(final JsonNode source) {
                        try {
                            return (T) ConversionServiceConfiguration.this.mapper.treeToValue(source, targetType);
                        } catch (JsonProcessingException e) {
                            throw new IllegalStateException("Failed converting the json to a new object instance.", e);
                        }
                    }
                };
            }
        };
    }
}
