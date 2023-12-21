package com.mahitotsu.cyclops.webapp.docdb;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mahitotsu.cyclops.webapp.TestContainerConfiguration;

@DataJpaTest(properties = """
        spring.jpa.generate-ddl=true
        """, showSql = true)
@AutoConfigureTestDatabase(replace = Replace.NONE)
@Import({ TestContainerConfiguration.class })
public class ItemEntityTest {

    @Autowired
    private TestEntityManager entityManager;

    @Test
    public void testCRUD() {

        // new entity
        final ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("key", "value");
        final ItemEntity e0 = new ItemEntity();
        e0.setJsonNode(json);
        assertNull(e0.getCreatedDate());
        assertNull(e0.getLastModifiedDate());

        // create
        final ItemEntity e1 = this.entityManager.persistFlushFind(e0);
        assertEquals(json, e1.getValue());
        assertNotNull(e1.getCreatedDate());
        assertNotNull(e1.getLastModifiedDate());

        // modifiy
        json.put("key", "newValue");
        assertNotEquals(json, e1.getValue());
        ObjectNode.class.cast(e1.getValue()).put("key", "newValue");
        assertEquals(json, e1.getValue());

        // update
        final ItemEntity e2 = this.entityManager.persistFlushFind(e1);
        assertEquals(json, e2.getValue());
        assertTrue(e2.getCreatedDate().equals(e1.getCreatedDate()));
        assertTrue(e2.getLastModifiedDate().isAfter(e1.getLastModifiedDate()));
        assertTrue(e2.getLastModifiedDate().isAfter(e2.getCreatedDate()));

        // delete
        this.entityManager.remove(e2);
        this.entityManager.flush();

        // select - afeter delete
        final ItemEntity e3 = this.entityManager.find(ItemEntity.class, e1.getId());
        assertNull(e3);
    }
}
