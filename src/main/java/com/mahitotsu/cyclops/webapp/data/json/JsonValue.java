package com.mahitotsu.cyclops.webapp.data.json;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
@EqualsAndHashCode
@ToString
public abstract class JsonValue<T> {
    public Class<?> getJavaType() {
        return this.getClass();
    }
}
