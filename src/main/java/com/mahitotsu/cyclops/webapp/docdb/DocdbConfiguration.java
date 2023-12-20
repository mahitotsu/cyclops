package com.mahitotsu.cyclops.webapp.docdb;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.ConversionService;

import jakarta.annotation.PostConstruct;

@Configuration
public class DocdbConfiguration {

    @Autowired
    private ConversionService conversionService;

    @PostConstruct
    public void setup() {
        ItemEntity.setConversionService(this.conversionService);
    }
}
