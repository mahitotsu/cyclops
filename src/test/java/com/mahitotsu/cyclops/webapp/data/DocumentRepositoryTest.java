package com.mahitotsu.cyclops.webapp.data;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import com.mahitotsu.cyclops.webapp.TestContainerConfiguration;

import lombok.Data;

@JdbcTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ TestContainerConfiguration.class, DocumentRepository.class })
public class DocumentRepositoryTest {

    @Autowired
    private DocumentRepository documentRepository;

    @Data
    public static class TestBean {
        private String id;
        private String text;
        private int number;
    }

    @Test
    public void testCRUD() {

        // init value
        final TestBean v0 = new TestBean();
        v0.setId(UUID.randomUUID().toString());
        v0.setText("string");
        v0.setNumber(128);

        //
        assertFalse(this.documentRepository.exists(v0.getId()));

        // Create
        this.documentRepository.create(v0, "id");
        assertTrue(this.documentRepository.exists(v0.getId()));

        // Read
        final TestBean v1 = this.documentRepository.findById(v0.getId(), TestBean.class);
        assertEquals(v0, v1);

        // Update
        v1.setText("text");
        v1.setNumber(-128);
        this.documentRepository.update(v1, "id");

        // Read
        final TestBean v2 = this.documentRepository.findById(v1.getId(), TestBean.class);
        assertEquals(v1, v2);

        // Delete
        this.documentRepository.deleteById(v2.getId());
        assertFalse(this.documentRepository.exists(v2.getId()));
    }

    @Test
    public void testUpsert() {

        // init value
        final TestBean v0 = new TestBean();
        v0.setId(UUID.randomUUID().toString());
        v0.setText("string");
        v0.setNumber(128);

        //
        assertFalse(this.documentRepository.exists(v0.getId()));

        // Create
        this.documentRepository.save(v0, "id");
        assertTrue(this.documentRepository.exists(v0.getId()));

        // Read
        final TestBean v1 = this.documentRepository.findById(v0.getId(), TestBean.class);
        assertEquals(v0, v1);

        // Update
        v1.setText("text");
        v1.setNumber(-128);
        this.documentRepository.save(v1, "id");

        // Read
        final TestBean v2 = this.documentRepository.findById(v1.getId(), TestBean.class);
        assertEquals(v1, v2);
    }
}
