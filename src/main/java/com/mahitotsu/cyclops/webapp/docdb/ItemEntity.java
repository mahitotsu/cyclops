package com.mahitotsu.cyclops.webapp.docdb;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDateTime;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Version;

@Entity
@Table(name = "items")
@EntityListeners({ AuditingEntityListener.class })
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

    @CreatedDate
    @Column(name = "created_date", nullable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Instant createdDate;

    @LastModifiedDate
    @Column(name = "modified_date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Instant lastModifiedDate;

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

    public Instant getCreatedDate() {
        return this.createdDate;
    }

    public Instant getLastModifiedDate() {
        return this.lastModifiedDate;
    }
}
