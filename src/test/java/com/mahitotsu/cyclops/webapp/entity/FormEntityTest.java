package com.mahitotsu.cyclops.webapp.entity;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.core.convert.ConversionService;

import com.fasterxml.jackson.databind.JsonNode;
import com.mahitotsu.cyclops.webapp.TestContainerConfiguration;

import lombok.Data;

@DataJpaTest(properties = "spring.jpa.generate-ddl=true", showSql = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestContainerConfiguration.class)
public class FormEntityTest {

    @Data
    public static class TestForm {
        private String number;
        private Date createdDate;
        private Status status;
        private List<Item> items;
        private boolean marked;
    }

    @Data
    public static class Item {
        private String key;
        private String value;
    }

    public static enum Status {
        SUBMITTED, ACCEPTED, COMPLETED, CANCELED,
    }

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ConversionService converter;

    @Test
    public void testCRUD() {

        // create a new form
        final TestForm form = new TestForm();
        form.setNumber(UUID.randomUUID().toString());
        form.setCreatedDate(new Date());
        form.setStatus(Status.SUBMITTED);
        form.setMarked(false);
        form.setItems(new ArrayList<>());
        for (int i = 0; i < 10; i++) {
            final Item item = new Item();
            item.setKey("key" + i);
            item.setValue("val" + i);
            form.getItems().add(item);
        }

        // prepare a new entity
        final FormEntity e0 = new FormEntity();
        e0.setData(this.converter.convert(form, JsonNode.class));
        assertNull(e0.getId());
        assertEquals(0, e0.getVersion());

        // persist
        final FormEntity e1 = this.entityManager.persistAndFlush(e0);
        assertNotNull(e1.getId());
        assertEquals(0, e1.getVersion());
        assertTrue(e0.getData().equals(e1.getData()));

        // update
        form.setStatus(Status.ACCEPTED);
        form.setMarked(true);
        for (final ListIterator<Item> i = form.getItems().listIterator(); i.hasNext();) {
            final int index = i.nextIndex();
            final Item item = i.next();
            if (index % 3 == 0) {
                i.remove();
            } else if (index % 2 == 0) {
                item.setKey(item.getKey().toUpperCase());
                item.setValue(item.getValue().toUpperCase());
            }
        }
        e1.setData(this.converter.convert(form, JsonNode.class));
        final FormEntity e2 = this.entityManager.persistAndFlush(e1);
        assertTrue(e1.getId().equals(e2.getId()));
        assertEquals(1, e1.getVersion());

        // clear local cache to ensure that the select statement is issued
        this.entityManager.clear(); 

        // select by primary key
        final FormEntity e3 = this.entityManager.find(FormEntity.class, e2.getId()); 
        assertEquals(e2, e3);
        assertEquals(form, this.converter.convert(e3.getData(), TestForm.class));
    }
}