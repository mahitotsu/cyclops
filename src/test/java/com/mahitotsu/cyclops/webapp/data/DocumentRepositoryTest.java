package com.mahitotsu.cyclops.webapp.data;

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
        private String text;
        private int number;
    }

    @Test
    public void testCRUD() {

        // init value
        final TestBean v0 = new TestBean();
        v0.setText("string");
        v0.setNumber(128);

        // 
        final String primaryKey = UUID.randomUUID().toString();

        // Create
        this.documentRepository.create(v0, primaryKey);

        // Read
        final TestBean v1 = this.documentRepository.read(primaryKey, TestBean.class);
        assertEquals(v0, v1);
    }
}
