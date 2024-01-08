package com.mahitotsu.cyclops.webapp.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mahitotsu.cyclops.webapp.data.validation.ForUpdate;

import jakarta.persistence.Entity;
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

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void test_AssignIdAndVersion() {

        // initialize
        final TestEntity e0 = new TestEntity();
        e0.setValue("v0");
        assertNull(e0.getId());
        assertNull(e0.getVersion());

        // create
        final TestEntity e1 = this.entityManager.persistFlushFind(e0);
        assertNotNull(e1.getId());
        assertNotNull(e1.getVersion());
    }

    @Test
    public void test_Auditing() {

        // initialize
        final TestEntity e0 = new TestEntity();
        e0.setValue("v0");
        assertNull(e0.getCreatedBy());
        assertNull(e0.getCreatedDateTime());
        assertNull(e0.getLastModifiedBy());
        assertNull(e0.getLastModifiedDateTime());

        // create
        final TestEntity e1 = this.entityManager.persistFlushFind(e0);
        assertNotNull(e1.getCreatedBy());
        assertNotNull(e1.getCreatedDateTime());
        assertNotNull(e1.getLastModifiedBy());
        assertNotNull(e1.getLastModifiedDateTime());
        assertTrue(e1.getCreatedDateTime().equals(e1.getLastModifiedDateTime()));

        // update
        e1.setValue("v1");
        final TestEntity e2 = this.entityManager.persistFlushFind(e1);
        assertTrue(e2.getCreatedDateTime().isBefore(e2.getLastModifiedDateTime()));
    }
}
