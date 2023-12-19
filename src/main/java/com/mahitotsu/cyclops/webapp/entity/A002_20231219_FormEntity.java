package com.mahitotsu.cyclops.webapp.entity;

import com.mahitotsu.cyclops.webapp.pojo.A002_20231219_AccountClose;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue(A002_20231219_AccountClose.FORMAT)
public class A002_20231219_FormEntity extends FormEntity<A002_20231219_AccountClose> {

    public A002_20231219_FormEntity() {
        super(A002_20231219_AccountClose.class);
    }
}
