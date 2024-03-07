package com.mahitotsu.cyclops.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Constructor;
import java.util.Set;

import org.junit.jupiter.api.Test;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import jakarta.validation.executable.ExecutableValidator;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

public class ValueObjectTest {

    @Getter(AccessLevel.PUBLIC)
    @EqualsAndHashCode
    @ToString
    public static class TestValue {

        private static ExecutableValidator ev = Validation.buildDefaultValidatorFactory().getValidator()
                .forExecutables();

        @SuppressWarnings("unchecked")
        public TestValue(@NotEmpty @Size(min = 3, max = 3) final String p1, @Min(0) @Max(128) final int p2) {

            final Constructor<?> init = this.getClass().getConstructors()[0];
            final Set<?> violations =  ev.validateConstructorParameters(init, new Object[] { p1, p2 });
            if (violations.isEmpty() == false) {
                throw new ConstraintViolationException((Set<ConstraintViolation<?>>) violations);
            }

            this.value = String.format("%s-%03d", p1.toUpperCase(), p2);
        }

        private String value;
    }

    @Test
    public void test_NewValue() {
        final TestValue v1 = new TestValue("abc", 12);
        assertEquals("ABC-012", v1.getValue());
    }

    @Test
    public void test_NewValueWithP1Null() {
        assertThrows(ConstraintViolationException.class, () -> new TestValue(" ", 12));
    }

    @Test
    public void test_NewValueWithP2Negative() {
        assertThrows(ConstraintViolationException.class, () -> new TestValue("abc", -12));
    }
}
