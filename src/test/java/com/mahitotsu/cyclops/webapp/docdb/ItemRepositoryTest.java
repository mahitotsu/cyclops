package com.mahitotsu.cyclops.webapp.docdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import java.io.Serializable;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import com.mahitotsu.cyclops.webapp.TestContainerConfiguration;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.Data;

@DataJpaTest(properties = """
        spring.jpa.generate-ddl=true
        """, showSql = true)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({
        TestContainerConfiguration.class,
})
public class ItemRepositoryTest {

    @Autowired
    private ItemRepository repository;

    @Entity
    @DiscriminatorValue("TestItem")
    public static class TestItemEntity extends ItemEntity<TestItem> {
        public TestItemEntity() {
            super(TestItem.class);
        }
    }

    @Data
    public static class TestItem {
        private String key1;
    }

    @Test
    public void testCRUD() {

        final TestItem item = new TestItem();
        final TestItemEntity e0 = new TestItemEntity();

        // initialize entity
        item.setKey1("value1");
        e0.setValue(item);

        // create
        final Serializable id = this.repository.putItem(e0).getId();
        this.repository.flush();

        //
        final ItemEntity<?> e1 = this.repository.findById(id).get();
        assertInstanceOf(TestItemEntity.class, e1);
        assertEquals(item, e1.getValue());
    }
}
