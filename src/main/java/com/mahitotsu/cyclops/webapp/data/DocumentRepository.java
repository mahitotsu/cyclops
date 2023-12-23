package com.mahitotsu.cyclops.webapp.data;

import java.sql.SQLException;

import org.postgresql.util.PGobject;
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

    private PGobject toJson(final Object value) {
        try {
            final PGobject json = new PGobject();
            json.setType("jsonb");
            json.setValue(this.mapper.writeValueAsString(value));
            return json;
        } catch (JsonProcessingException | SQLException e) {
            throw new IllegalStateException("Conversion from object to JSON string failed.", e);
        }
    }

    private <T> T toValue(final PGobject json, final Class<T> type) {
        try {
            return this.mapper.readValue(json.getValue(), type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Conversion from JSON string to object failed.", e);
        }
    }

    @Transactional
    public void create(final Object value, final String primaryKeyName) {

        if (value == null || primaryKeyName == null) {
            return;
        }

        this.client.update("""
                INSERT INTO items (pk, item) VALUES (:json->>:pk, :json)
                """,
                new MapSqlParameterSource()
                        .addValue("pk", primaryKeyName)
                        .addValue("json", this.toJson(value)));
    }

    @Transactional
    public void update(final Object value, final String primaryKeyName) {

        if (value == null || primaryKeyName == null) {
            return;
        }

        this.client.update("""
                UPDATE items SET item = :json where pk = :json->>:pk
                """,
                new MapSqlParameterSource()
                        .addValue("pk", primaryKeyName)
                        .addValue("json", this.toJson(value)));
    }

    @Transactional
    public void save(final Object value, final String primaryKeyName) {

        if (value == null || primaryKeyName == null) {
            return;
        }

        this.client.update("""
                INSERT INTO items (pk, item) VALUES (:json->>:pk, :json)
                ON CONFLICT (pk)
                DO UPDATE SET item = :json
                """,
                new MapSqlParameterSource()
                        .addValue("pk", primaryKeyName)
                        .addValue("json", this.toJson(value)));
    }

    @Transactional(readOnly = true)
    public <T> T findById(final String primaryKeyValue, final Class<T> type) {

        if (primaryKeyValue == null) {
            return null;
        }

        final PGobject json = this.client.queryForObject("""
                SELECT item FROM items WHERE pk = :pk
                        """,
                new MapSqlParameterSource()
                        .addValue("pk", primaryKeyValue),
                PGobject.class);
        return this.toValue(json, type);
    }

    @Transactional(readOnly = true)
    public boolean exists(final String primaryKeyValue) {

        if (primaryKeyValue == null) {
            return false;
        }

        final Integer count = this.client.queryForObject("""
                SELECT count(pk) FROM items WHERE pk = :pk
                        """,
                new MapSqlParameterSource()
                        .addValue("pk", primaryKeyValue),
                Integer.class);
        return count != null && count > 0;
    }

    @Transactional
    public void deleteById(final String primaryKeyValue) {

        if (primaryKeyValue == null) {
            return;
        }

        this.client.update("""
                DELETE FROM items where pk = :pk
                """,
                new MapSqlParameterSource()
                        .addValue("pk", primaryKeyValue));
    }
}