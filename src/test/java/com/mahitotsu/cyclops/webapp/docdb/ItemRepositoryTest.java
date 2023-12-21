package com.mahitotsu.cyclops.webapp.docdb;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
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
    private ItemRepository itemRepository;

    @Autowired
    private TestEntityManager entityManager;

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
        item.setKey1("value1");

        final TestItemEntity entity = new TestItemEntity();
        entity.setValue(item);

        // create
        this.itemRepository.save(entity);
        this.entityManager.flush();
        this.entityManager.clear();

        // select - afeter create
        final ItemEntity<?> e1 = this.itemRepository.findById(entity.getId()).get();
        assertEquals(item, e1.getValue());
        this.entityManager.clear();
    }
}
