package com.mahitotsu.cyclops.webapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.function.Supplier;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public class JpaAuditingTest extends AbstractDataTestBase {

    @Autowired
    private TestEntityManager entityManager;

    @Entity
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TestEntity extends AbstractEntityBase<Long> {
        private String text;

        @Override
        protected Supplier<Long> idSupplier() {
            return () -> System.nanoTime();
        }
    }

    @Test
    public void testAuditing_CreateAndModify() {

        // initialize
        final TestEntity e0 = new TestEntity();
        e0.setText("v0");
        assertNull(e0.getId());
        assertNull(e0.getVersion());
        assertNull(e0.getCreatedDateTime());
        assertNull(e0.getCreatedBy());
        assertNull(e0.getLastModifiedDateTime());
        assertNull(e0.getLastModifiedBy());

        // create
        final TestEntity e1 = this.entityManager.persistFlushFind(e0);
        assertNotNull(e1.getId());
        assertEquals(Long.valueOf(0), e1.getVersion());
        assertNotNull(e1.getCreatedDateTime());
        assertNotNull(e1.getLastModifiedDateTime());
        assertTrue(e1.getCreatedDateTime().equals(e1.getLastModifiedDateTime()));
        assertNotNull(e1.getCreatedBy());
        assertNotNull(e1.getLastModifiedBy());
        assertTrue(e1.getCreatedBy().equals(e1.getLastModifiedBy()));

        // modify
        e1.setText("v1");
        final TestEntity e2 = this.entityManager.persistFlushFind(e1);
        assertEquals(Long.valueOf(1), e2.getVersion());
        assertTrue(e1.getCreatedDateTime().isBefore(e1.getLastModifiedDateTime()));
        assertTrue(e1.getCreatedBy().equals(e1.getLastModifiedBy()));
    }
}
