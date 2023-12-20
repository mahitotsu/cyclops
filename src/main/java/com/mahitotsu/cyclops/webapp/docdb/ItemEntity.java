package com.mahitotsu.cyclops.webapp.docdb;

import java.io.Serializable;

import org.hibernate.annotations.Type;
import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

@Entity
@Table(name = "items")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "type", discriminatorType = DiscriminatorType.STRING)
public abstract class ItemEntity<T> {

    private static ConversionService conversionService;

    public static void setConversionService(final ConversionService conversionService) {
        ItemEntity.conversionService = conversionService;
    }

    protected ItemEntity(final Class<T> itemType) {
        this.itemType = itemType;
    }

    @Transient
    private T value;

    @Transient
    private Class<T> itemType;

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Type(JsonBinaryType.class)
    @Column(name = "data", columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JsonNode data;

    public Serializable getId() {
        return this.id;
    }

    public T getValue() {
        if (this.value == null) {
            this.jsonToValue();
        }
        return this.value;
    }

    public void setValue(final T value) {
        this.value = value;
    }

    @PrePersist
    @PreUpdate
    private void valueToJson() {
        this.data = (this.value == null ? null : ItemEntity.conversionService.convert(this.value, JsonNode.class));
    }

    private void jsonToValue() {
        this.value = (this.data == null ? null : ItemEntity.conversionService.convert(this.data, this.itemType));
    }
}
