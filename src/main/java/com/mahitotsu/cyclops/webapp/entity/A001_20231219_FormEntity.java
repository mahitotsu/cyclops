package com.mahitotsu.cyclops.webapp.entity;

import com.mahitotsu.cyclops.webapp.pojo.A001_20231219_AccountOpen_Form;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(A001_20231219_AccountOpen_Form.FORMAT)
public class A001_20231219_FormEntity extends FormEntity<A001_20231219_AccountOpen_Form> {

    public A001_20231219_FormEntity() {
        super(A001_20231219_AccountOpen_Form.class);
    }
}
