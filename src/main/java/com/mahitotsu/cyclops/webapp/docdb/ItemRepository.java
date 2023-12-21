package com.mahitotsu.cyclops.webapp.docdb;

import java.io.Serializable;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<ItemEntity<?>, Serializable>, ItemDao {

}