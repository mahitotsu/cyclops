package com.mahitotsu.cyclops.webapp.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public class JpaCommonTest extends AbstractDataTestBase {

    @Entity
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TestEntity extends AbstractEntityBase {
        private String text;
    }

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void test_Auditing() {

        // init
        final TestEntity e0 = new TestEntity();
        e0.setText("v1");
        assertNull(e0.getId());
        assertNull(e0.getModCount());
        assertNull(e0.getCreatedDate());
        assertNull(e0.getCreatedBy());
        assertNull(e0.getLastModifiedDate());
        assertNull(e0.getLastModifiedBy());

        // create
        final TestEntity e1 = this.entityManager.persistFlushFind(e0);
        assertNotNull(e1.getId());
        assertNotNull(e1.getModCount());
        assertNotNull(e1.getCreatedDate());
        assertNotNull(e1.getCreatedBy());
        assertNotNull(e1.getLastModifiedDate());
        assertNotNull(e1.getLastModifiedBy());
        assertTrue(e1.getCreatedDate().equals(e1.getLastModifiedDate()));

        // update
        e1.setText("v2");
        final TestEntity e2 = this.entityManager.persistFlushFind(e1);
        assertTrue(e2.getCreatedDate().isBefore(e2.getLastModifiedDate()));
    }
}
