package com.mahitotsu.cyclops.webapp.docdb;

public interface ItemDao {
    <E extends ItemEntity<?>> E putItem(E entity);
}
