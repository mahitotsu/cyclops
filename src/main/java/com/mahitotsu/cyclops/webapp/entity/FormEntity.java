package com.mahitotsu.cyclops.webapp.entity;

import org.hibernate.annotations.Type;
import org.springframework.util.Assert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mahitotsu.cyclops.webapp.pojo.Form;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;

@Entity
@Table(name = "forms")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "format")
public abstract class FormEntity<F extends Form> {

    private static final ObjectMapper CONVERTER = new ObjectMapper();

    protected FormEntity(final Class<F> type) {
        Assert.notNull(type, "The type must not be null.");
        this.type = type;
    }

    @Transient
    private Class<F> type;
    @Transient
    private F form;

    @Id
    @GeneratedValue
    @Column(name = "id", updatable = false)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    @Column(name = "format", nullable = false, insertable = false, updatable = false)
    private String format;

    @Type(JsonBinaryType.class)
    @Column(name = "data", columnDefinition = "jsonb")
    private JsonNode data;

    public Long getId() {
        return this.id;
    }

    public String getFormat() {
        return this.format;
    }

    public F getForm() {
        return this.form;
    }

    public void setForm(final F form) {
        this.form = form;
    }

    @PrePersist
    @PreUpdate
    private void onSave() {
        this.data = this.form == null ? null : CONVERTER.valueToTree(this.form);
    }

    @PostLoad
    private void onLoad() {
        try {
            this.form = this.data == null ? null : CONVERTER.treeToValue(this.data, this.type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("An error occurred while converting JSON to Object.", e);
        }
    }
}
