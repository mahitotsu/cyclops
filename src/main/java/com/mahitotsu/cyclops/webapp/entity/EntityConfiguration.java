package com.mahitotsu.cyclops.webapp.entity;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(setDates = true, modifyOnCreate = true)
public class EntityConfiguration {
    
}
