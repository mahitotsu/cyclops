package com.mahitotsu.cyclops.webapp.docdb;

import java.io.Serializable;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    private static final ObjectMapper mapper = new ObjectMapper();

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
        this.data = (this.value == null ? null : ItemEntity.mapper.valueToTree(this.value));
    }

    private void jsonToValue() {
        try {
            this.value = (this.data == null ? null : ItemEntity.mapper.treeToValue(this.data, this.itemType));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Conversion from json to pojo failed.", e);
        }
    }
}
