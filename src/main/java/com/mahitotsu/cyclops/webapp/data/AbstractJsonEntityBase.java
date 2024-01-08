package com.mahitotsu.cyclops.webapp.data;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@MappedSuperclass
@RequiredArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AbstractJsonEntityBase<T> extends AbstractEntityBase {

    @Transient
    private final Class<T> valueType;

    @Transient
    private final ObjectMapper objectMapper = DataConfiguration.getManagedObjectMapper().get();

    @JdbcTypeCode(SqlTypes.JSON)
    @Getter
    @NotNull
    private String jsonValue;

    public T getValue() {
        if (this.jsonValue == null) {
            return null;
        }
        try {
            return this.objectMapper.readValue(this.jsonValue, this.valueType);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public void setValue(final T value) {
        if (value == null) {
            this.jsonValue = null;
            return;
        }
        if (this.valueType.equals(value.getClass()) == false) {
            throw new IllegalArgumentException("The specified value is not acceptable type.");
        }
        try {
            this.jsonValue = this.objectMapper.writeValueAsString(value);
        } catch (final JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
