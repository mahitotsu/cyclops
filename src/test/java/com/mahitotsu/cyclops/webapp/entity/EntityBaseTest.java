package com.mahitotsu.cyclops.webapp.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EntityTest
public class EntityBaseTest {

    @Entity
    @Data
    @EqualsAndHashCode(callSuper = true)
    public static class TestEntity extends EntityBase {
        private String value;
    }

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testEqualsAndHashCode() {

        //
        final TestEntity e0 = new TestEntity();
        final TestEntity e1 = new TestEntity();

        //
        assertNotSame(e0, e1);
        assertEquals(e0, e1);
        assertEquals(e0.hashCode(), e1.hashCode());
    }

    @Test
    public void testCRUD() {

        //
        final TestEntity e0 = new TestEntity();
        e0.setValue("v0");
        assertNull(e0.getId());
        assertNull(e0.getCreatedTimestamp());
        assertNull(e0.getLastModifiedTimepstamp());

        // create
        final TestEntity e1 = this.entityManager.persistFlushFind(e0);
        assertNotNull(e1);
        assertNotSame(e0, e1);
        assertNotNull(e1.getId());
        assertNotNull(e1.getCreatedTimestamp());
        assertNotNull(e1.getLastModifiedTimepstamp());
        assertTrue(e1.getCreatedTimestamp().equals(e1.getLastModifiedTimepstamp()));

        // update
        {
            // Update the value so that an update statement needs to be executed.
            e1.setValue("v1");
        }
        final TestEntity e2 = this.entityManager.persistFlushFind(e1);
        assertNotNull(e2);
        assertNotSame(e1, e2);
        {
            // This transaction has not yet been committed,
            // so the version will not be incremented.
            assertEquals(e1.getVersion(), e2.getVersion());
        }
        assertEquals(e1.getCreatedTimestamp(), e2.getCreatedTimestamp());
        assertNotEquals(e1.getLastModifiedTimepstamp(), e2.getLastModifiedTimepstamp());
        assertTrue(e2.getLastModifiedTimepstamp().isAfter(e2.getCreatedTimestamp()));

        // delete
        this.entityManager.remove(e2);
        this.entityManager.flush();
        final TestEntity e3 = this.entityManager.find(TestEntity.class, e2.getId());
        assertNull(e3);
    }
}
