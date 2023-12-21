package com.mahitotsu.cyclops.webapp.docdb;

import java.io.Serializable;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "items")
public class ItemEntity {

    @Id
    @GeneratedValue
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Type(JsonBinaryType.class)
    @Column(name = "value", columnDefinition = "jsonb")
    @Basic(fetch = FetchType.LAZY)
    private JsonNode value;

    public Serializable getId() {
        return this.id;
    }

    public Comparable<?> getVersion() {
        return this.version;
    }

    public JsonNode getValue() {
        return this.value;
    }

    public void setJsonNode(final JsonNode value) {
        this.value = value;
    }
}
