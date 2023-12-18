package com.mahitotsu.cyclops.webapp.entity;

import org.hibernate.annotations.Type;

import com.fasterxml.jackson.databind.JsonNode;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;

@Entity
public class FormEntity {
    
    @Id
    @GeneratedValue
    private Long id;

    @Version
    private long version;

    @Type(JsonBinaryType.class)
    private JsonNode data;
}
