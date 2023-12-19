package com.mahitotsu.cyclops.webapp.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class A001_20231219_AccountOpen_Form extends Form {

    public static final String FORMAT = "A001_20231219";

    private Customer customer;
}
