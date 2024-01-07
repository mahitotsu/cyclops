package com.mahitotsu.cyclops.webapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Transient;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

public class JsonbColumnTest extends AbstractDataTestBase {

    @Entity
    @Inheritance(strategy = InheritanceType.SINGLE_TABLE)
    @NoArgsConstructor
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static abstract class TestEntity<T> extends AbstractEntityBase {

        @Transient
        private ObjectMapper objectMapper;

        private ObjectMapper getObjectMapper() {
            if (this.objectMapper == null) {
                this.objectMapper = new ObjectMapper();
            }
            return this.objectMapper;
        }

        @JdbcTypeCode(SqlTypes.JSON)
        @Getter
        @NotNull
        private String jsonValue;

        protected abstract Class<T> getValueType();

        public T getValue() {
            try {
                return this.getObjectMapper().readValue(this.jsonValue, this.getValueType());
            } catch (final JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }

        public void setValue(final T value) {
            try {
                this.jsonValue = this.getObjectMapper().writeValueAsString(value);
            } catch (final JsonProcessingException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    @Data
    public static class TextValue {
        private String item;
    }

    @Data
    public static class LongValue {
        private long item;
    }

    @Entity
    public static class TextEntity extends TestEntity<TextValue> {
        public Class<TextValue> getValueType() {
            return TextValue.class;
        }
    }

    @Entity
    public static class LongEntity extends TestEntity<LongValue> {
        public Class<LongValue> getValueType() {
            return LongValue.class;
        }
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    public void test_CreateUpdateJsonColumn() {

        // initialize
        final TextValue t0 = new TextValue();
        t0.setItem("v0");
        final TextEntity e0 = new TextEntity();
        e0.setValue(t0);

        // create
        this.entityManager.persist(e0);
        this.entityManager.flush();
        this.entityManager.detach(e0);

        // find created
        final TextEntity e1 = this.entityManager.find(TextEntity.class, e0.getId());
        final TextValue t1 = e1.getValue();
        assertEquals("v0", t1.getItem());

        // update
        t1.setItem("v1");
        e1.setValue(t1);
        this.entityManager.flush();
        this.entityManager.detach(e1);

        // find updated
        final TextEntity e2 = this.entityManager.find(TextEntity.class, e1.getId());
        final TextValue t2 = e2.getValue();
        assertEquals("v1", t2.getItem());
    }

    @Test
    @Transactional
    public void test_StoreMultiTypes() {

        // initialize -text
        final TextValue t0 = new TextValue();
        t0.setItem("v0");
        final TextEntity e0 = new TextEntity();
        e0.setValue(t0);

        // initialize -number
        final LongValue n0 = new LongValue();
        n0.setItem(0L);
        final LongEntity e1 = new LongEntity();
        e1.setValue(n0);

        // create
        this.entityManager.persist(e0);
        this.entityManager.persist(e1);
        this.entityManager.flush();
        this.entityManager.detach(e0);
        this.entityManager.detach(e1);

        // find all
        final List<?> entities = this.entityManager.createQuery("""
                select e from JsonbColumnTest$TestEntity e order by e.createdDateTime
                 """.trim())
                .getResultList();
        assertEquals(2, entities.size());
        assertInstanceOf(TextEntity.class, entities.get(0));
        assertInstanceOf(LongEntity.class, entities.get(1));
    }
}