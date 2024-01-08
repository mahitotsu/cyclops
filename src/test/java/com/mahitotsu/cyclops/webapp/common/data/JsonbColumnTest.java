package com.mahitotsu.cyclops.webapp.common.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import jakarta.persistence.Entity;
import lombok.Data;

public class JsonbColumnTest extends AbstractDataTestBase {

    @Data
    public static class TextValue implements Serializable {
        private String item;
    }

    @Data
    public static class LongValue implements Serializable {
        private long item;
    }

    @Data
    public static class DateTimeValue implements Serializable {
        private LocalDateTime item;
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

    @Entity
    public static class DateTimeEntity extends AbstractJsonEntity<DateTimeValue> {
        public DateTimeEntity() {
            super(DateTimeValue.class);
        }
    }

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void test_CreateUpdateJsonColumn() {

        // initialize
        final TextValue t0 = new TextValue();
        t0.setItem("v0");
        final TextEntity e0 = new TextEntity();
        e0.setValue(t0);

        // create
        final TextEntity e1 = this.entityManager.persistFlushFind(e0);
        final TextValue t1 = e1.getValue();
        assertEquals("v0", t1.getItem());

        // update
        t1.setItem("v1");
        e1.setValue(t1);
        final TextEntity e2 = this.entityManager.persistFlushFind(e1);
        final TextValue t2 = e2.getValue();
        assertEquals("v1", t2.getItem());
    }

    @Test
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

        // initialize - datetime
        final DateTimeValue d0 = new DateTimeValue();
        d0.setItem(LocalDateTime.now());
        final DateTimeEntity e2 = new DateTimeEntity();
        e2.setValue(d0);

        // create
        this.entityManager.persist(e0);
        this.entityManager.persist(e1);
        this.entityManager.persist(e2);
        this.entityManager.flush();
        this.entityManager.detach(e0);
        this.entityManager.detach(e1);
        this.entityManager.detach(e2);

        // find all
        final List<?> entities = this.entityManager.getEntityManager().createQuery("""
                select e from AbstractJsonEntity e order by e.createdDateTime
                 """.trim())
                .getResultList();
        assertEquals(3, entities.size());

        // check types
        assertInstanceOf(TextEntity.class, entities.get(0));
        assertInstanceOf(LongEntity.class, entities.get(1));
        assertInstanceOf(DateTimeEntity.class, entities.get(2));

        // check instances
        assertNotSame(e0, entities.get(0));
        assertNotSame(e1, entities.get(1));
        assertNotSame(e2, entities.get(2));
    }
}