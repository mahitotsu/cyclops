package com.mahitotsu.cyclops.webapp.data;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Repository
public class DocumentRepository {

    @Autowired
    private NamedParameterJdbcOperations client;

    private ObjectMapper mapper = new ObjectMapper();

    private String toJson(final Object value) {
        try {
            return this.mapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Conversion from object to JSON string failed.", e);
        }
    }

    private <T> T toValue(final String json, final Class<T> type) {
        try {
            return this.mapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Conversion from JSON string to object failed.", e);
        }
    }

    @Transactional
    public void create(final Object value, final String primaryKey) {

        if (value == null || primaryKey == null) {
            return;
        }

        this.client.update("""
                INSERT INTO items (pk, value) VALUES (:pk, :json::JSONB)
                """,
                new MapSqlParameterSource()
                        .addValue("pk", primaryKey)
                        .addValue("json", this.toJson(value)));
    }

    @Transactional(readOnly = true)
    public <T> T read(final String primaryKey, final Class<T> type) {

        if (primaryKey == null) {
            return null;
        }

        final String json = this.client.queryForObject("""
                SELECT value FROM items WHERE pk = :pk
                        """,
                new MapSqlParameterSource()
                        .addValue("pk", primaryKey),
                String.class);
        return this.toValue(json, type);
    }

    @Transactional(readOnly = true)
    public boolean exists(final String primaryKey) {

        if (primaryKey == null) {
            return false;
        }

        final Integer count = this.client.queryForObject("""
                SELECT count(pk) FROM items WHERE pk = :pk
                        """,
                new MapSqlParameterSource()
                        .addValue("pk", primaryKey),
                Integer.class);
        return count != null && count > 0;
    }
}
