package com.mahitotsu.cyclops.webapp.pojo;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class A002_20231219_AccountClose_Form extends Form {

    public static final String FORMAT = "A002_20231219";

    private Customer customer;
    private String accountNumber;
}
