package com.mahitotsu.cyclops.webapp.common.data;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.List;

import org.junit.jupiter.api.Test;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.Data;

public class JsonbColumnTest extends AbstractDataTestBase {

    @Data
    public static class TextValue {
        private String item;
    }

    @Data
    public static class LongValue {
        private long item;
    }

    @Entity
    public static class TextEntity extends AbstractJsonEntity<TextValue> {
        public TextEntity() {
            super(TextValue.class);
        }
    }

    @Entity
    public static class LongEntity extends AbstractJsonEntity<LongValue> {
        public LongEntity() {
            super(LongValue.class);
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
                select e from AbstractJsonEntity e order by e.createdDateTime
                 """.trim())
                .getResultList();
        assertEquals(2, entities.size());
        assertInstanceOf(TextEntity.class, entities.get(0));
        assertInstanceOf(LongEntity.class, entities.get(1));
    }
}