package com.mahitotsu.cyclops.webapp.common.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import com.mahitotsu.cyclops.webapp.common.data.validation.ForUpdate;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

public class EntityBaseTest extends AbstractDataTestBase {

    @Entity
    @NoArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TestEntity extends AbstractEntityBase {
        @NotNull
        @NotBlank(groups = { ForUpdate.class })
        private String value;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Test
    @Transactional
    public void test_AssignIdAndVersion() {

        // initialize
        final TestEntity e0 = new TestEntity();
        e0.setValue("v0");
        assertNull(e0.getId());
        assertNull(e0.getVersion());

        // create
        this.entityManager.persist(e0);
        this.entityManager.flush();
        TestEntity e1 = this.entityManager.find(TestEntity.class, e0.getId());
        assertNotNull(e1.getId());
        assertNotNull(e1.getVersion());
    }

    @Test
    @Transactional
    public void test_Auditing() {

        // initialize
        final TestEntity e0 = new TestEntity();
        e0.setValue("v0");
        assertNull(e0.getCreatedBy());
        assertNull(e0.getCreatedDateTime());
        assertNull(e0.getLastModifiedBy());
        assertNull(e0.getLastModifiedDateTime());

        // create
        this.entityManager.persist(e0);
        this.entityManager.flush();
        TestEntity e1 = this.entityManager.find(TestEntity.class, e0.getId());
        assertNotNull(e1.getCreatedBy());
        assertNotNull(e1.getCreatedDateTime());
        assertNotNull(e1.getLastModifiedBy());
        assertNotNull(e1.getLastModifiedDateTime());
        assertTrue(e1.getCreatedDateTime().equals(e1.getLastModifiedDateTime()));

        // update
        e1.setValue("v1");
        this.entityManager.flush();
        TestEntity e2 = this.entityManager.find(TestEntity.class, e1.getId());
        assertTrue(e2.getCreatedDateTime().isBefore(e2.getLastModifiedDateTime()));
    }
}
