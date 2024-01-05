package com.mahitotsu.cyclops.webapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertThrows;

import java.util.Arrays;
import java.util.List;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import jakarta.persistence.Entity;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Query;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public class JsonbTypeTest extends AbstractDataTestBase {

    @Entity
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TestEntity extends AbstractEntityBase {
        @JdbcTypeCode(SqlTypes.JSON)
        private JsonValue jsonValue;
    }

    @Data
    public static class JsonValue {
        private String text;
        private int number;
        private List<JsonValue> children;
    }

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void test_CrudJsonAttribute() {

        // init
        final JsonValue c0 = new JsonValue();
        c0.setNumber(0);
        final JsonValue c1 = new JsonValue();
        c0.setNumber(1);

        final JsonValue j0 = new JsonValue();
        j0.setText("p01");
        j0.setChildren(Arrays.asList(c0, c1));

        final TestEntity e0 = new TestEntity();
        e0.setJsonValue(j0);

        // create
        final TestEntity e1 = this.entityManager.persistFlushFind(e0);
        assertEquals(j0, e1.getJsonValue());
        assertEquals(2, e1.getJsonValue().getChildren().size());

        // modify
        final JsonValue j1 = e1.getJsonValue();
        j1.setText("p02");
        j1.getChildren().remove(1);
        assertFalse(j1.equals(j0));

        // update
        final TestEntity e2 = this.entityManager.persistFlushFind(e1);
        assertNotEquals(j0, e2.getJsonValue());
        assertEquals(j1, e2.getJsonValue());
    }

    @Test
    public void test_QueryJsonValueByJsonObject() {

        // init
        final JsonValue j0 = new JsonValue();
        j0.setText("p01");
        final TestEntity e0 = new TestEntity();
        e0.setJsonValue(j0);

        final JsonValue j1 = new JsonValue();
        j1.setText("p02");
        final TestEntity e1 = new TestEntity();
        e1.setJsonValue(j1);

        // create
        this.entityManager.persist(e0);
        this.entityManager.persist(e1);
        this.entityManager.flush();

        // query
        final Query query = this.entityManager.getEntityManager().createQuery("""
                select t from JsonbTypeTest$TestEntity t where t.jsonValue = :jsonValue
                                """, TestEntity.class);

        // query - match
        final TestEntity result = (TestEntity) query.setParameter("jsonValue", j1)
                .getSingleResult();
        assertEquals(j1, result.getJsonValue());

        // query - not match
        assertThrows(NoResultException.class, () -> {
            query.setParameter("jsonValue", new JsonValue()).getSingleResult();
        });
    }

    @Test
    public void test_QueryJsonValueByJsonKey() {

        // init
        final JsonValue j0 = new JsonValue();
        j0.setText("p01");
        final TestEntity e0 = new TestEntity();
        e0.setJsonValue(j0);

        final JsonValue j1 = new JsonValue();
        j1.setText("p02");
        final TestEntity e1 = new TestEntity();
        e1.setJsonValue(j1);

        // create
        this.entityManager.persist(e0);
        this.entityManager.persist(e1);
        this.entityManager.flush();

        // create query
        final Query query = this.entityManager.getEntityManager().createNativeQuery("""
                select * from jsonb_type_test$test_entity t where t.json_value->>'text' = :text
                """, TestEntity.class);

        // query - match
        final TestEntity result = (TestEntity) query
                .setParameter("text", j1.getText())
                .getSingleResult();
        assertEquals(j1, result.getJsonValue());

        // query - not match
        assertThrows(NoResultException.class, () -> {
            query.setParameter("text", "p03").getSingleResult();
        });
    }
}
