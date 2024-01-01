package com.mahitotsu.cyclops.webapp.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.mahitotsu.cyclops.webapp.data.validation.ForCreate;
import com.mahitotsu.cyclops.webapp.data.validation.ForUpdate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

public class JpaCommonTest extends AbstractDataTestBase {

    @Autowired
    private TestEntityManager entityManager;

    @Entity
    @Data
    @EqualsAndHashCode(callSuper = true)
    @ToString(callSuper = true)
    public static class TestEntity extends AbstractEntityBase {
        @Column(nullable = false)
        @NotNull(groups = { ForCreate.class }) // -> for instert, an empty string is allowed
        @NotBlank(groups = { ForUpdate.class }) // -> for update, an empty string is not allowed
        private String text;

        @Column(nullable = true)
        @Min(0) // -> If a value is set, it must always be a positive integer
        private Integer number;
    }

    @Test
    public void testAuditing_CreateAndModify() {

        // initialize
        final TestEntity e0 = new TestEntity();
        e0.setText("v0");
        e0.setNumber(0);
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
        e1.setNumber(1);
        final TestEntity e2 = this.entityManager.persistFlushFind(e1);
        assertEquals(Long.valueOf(1), e2.getVersion());
        assertTrue(e1.getCreatedDateTime().isBefore(e1.getLastModifiedDateTime()));
        assertTrue(e1.getCreatedBy().equals(e1.getLastModifiedBy()));
    }

    @Test
    public void testValidation_ForCreate() {

        // init
        final TestEntity e0 = new TestEntity();
        e0.setText(null);
        assertNull(e0.getText());

        // An exception will be thrown
        try {
            this.entityManager.persistAndFlush(e0);
            fail("The expected ConstraintViolationException was not thrown.");
        } catch (Exception e) {
            assertInstanceOf(ConstraintViolationException.class, e);
            final Set<ConstraintViolation<?>> violations = ConstraintViolationException.class.cast(e)
                    .getConstraintViolations();
            assertEquals(1, violations.size());
        }
    }

    @Test
    public void testValidation_ForDefault() {

        // init
        final TestEntity e0 = new TestEntity();
        e0.setText("");
        e0.setNumber(-1);
        assertNotNull(e0.getText());
        assertTrue(Integer.valueOf(0).compareTo(e0.getNumber()) > 0);

        // An exception will be thrown
        try {
            this.entityManager.persistAndFlush(e0);
            fail("The expected ConstraintViolationException was not thrown.");
        } catch (Exception e) {
            assertInstanceOf(ConstraintViolationException.class, e);
            final Set<ConstraintViolation<?>> violations = ConstraintViolationException.class.cast(e)
                    .getConstraintViolations();
            assertEquals(1, violations.size());
        }
    }

    @Test
    public void testValidation_ForUpdate() {

        // init
        final TestEntity e0 = new TestEntity();
        e0.setText("");
        assertNotNull(e0.getText());
        assertTrue(e0.getText().isEmpty());

        // create
        final TestEntity e1 = this.entityManager.persistFlushFind(e0);

        // update
        e1.setNumber(1); // update only number field. -> the text field is still empty.
        assertTrue(e1.getText().isEmpty());
        assertNotEquals(e0, e1);

        // An exception will be thrown
        try {
            this.entityManager.persistAndFlush(e1);
            fail("The expected ConstraintViolationException was not thrown.");
        } catch (Exception e) {
            assertInstanceOf(ConstraintViolationException.class, e);
            final Set<ConstraintViolation<?>> violations = ConstraintViolationException.class.cast(e)
                    .getConstraintViolations();
            assertEquals(1, violations.size());
        }
    }

    @Test
    public void testValidation_WithMultiViolation() {

        // init
        final TestEntity e0 = new TestEntity();
        e0.setText(null);
        e0.setNumber(-1);
        assertNull(e0.getText());
        assertTrue(Integer.valueOf(0).compareTo(e0.getNumber()) > 0);

        // An exception will be thrown
        try {
            this.entityManager.persistAndFlush(e0);
            fail("The expected ConstraintViolationException was not thrown.");
        } catch (Exception e) {
            assertInstanceOf(ConstraintViolationException.class, e);
            final Set<ConstraintViolation<?>> violations = ConstraintViolationException.class.cast(e)
                    .getConstraintViolations();
            assertEquals(2, violations.size());
        }
    }
}
