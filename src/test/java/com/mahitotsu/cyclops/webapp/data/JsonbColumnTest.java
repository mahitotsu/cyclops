package com.mahitotsu.cyclops.webapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.mahitotsu.cyclops.webapp.data.json.JsonValue;
import com.mahitotsu.cyclops.webapp.data.json.JsonValueConverter;

import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public class JsonbColumnTest extends AbstractDataTestBase {

    @Entity
    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TestEntity extends AbstractEntityBase {
        @JdbcTypeCode(SqlTypes.JSON)
        @Convert(converter = JsonValueConverter.class)
        @NotNull
        private JsonValue<?> value;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TextValue extends JsonValue<TextValue> {
        private String item;
    }

    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class LongValue extends JsonValue<LongValue> {
        private long item;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    public void test_MultiTypesInSingleTable() {

        // initialize - text
        final TextValue t0 = new TextValue();
        t0.setItem("v0");
        final TestEntity e0 = new TestEntity();
        e0.setValue(t0);

        // initialize - long
        final LongValue n0 = new LongValue();
        n0.setItem(0L);
        final TestEntity e1 = new TestEntity();
        e1.setValue(n0);

        // create
        this.entityManager.persist(e0);
        this.entityManager.persist(e1);
        this.entityManager.flush();

        // detach
        this.entityManager.detach(e0);
        this.entityManager.detach(e1);

        // query all entities
        final List<TestEntity> entities = this.entityManager.createQuery("""
                select e from JsonbColumnTest$TestEntity e
                    """, TestEntity.class)
                .getResultList();
        assertEquals(2, entities.size());

        // query by text value
        final TestEntity et = (TestEntity) this.entityManager.createNativeQuery("""
                select * from jsonb_column_test$test_entity e where e.value @> :condition\\:\\:jsonb
                    """, TestEntity.class)
                .setParameter("condition", "{\"item\": \"v0\"}")
                .getSingleResult();
        assertInstanceOf(TextValue.class, et.getValue());
        assertEquals("v0", TextValue.class.cast(et.getValue()).getItem());

        // query by long value
        final TestEntity en = (TestEntity) this.entityManager.createNativeQuery("""
                select * from jsonb_column_test$test_entity e where e.value @> :condition\\:\\:jsonb
                    """, TestEntity.class)
                .setParameter("condition", "{\"item\": 0}")
                .getSingleResult();
        assertInstanceOf(LongValue.class, en.getValue());
        assertEquals(0L, LongValue.class.cast(en.getValue()).getItem());
    }
}