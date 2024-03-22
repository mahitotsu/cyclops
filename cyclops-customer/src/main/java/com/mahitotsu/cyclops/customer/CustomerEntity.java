package com.mahitotsu.cyclops.customer;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Version;
import lombok.Data;

@Entity
@Data
public class CustomerEntity {
    
    @Id
    @GeneratedValue
    private Long id;

    @Version
    private Long version;

    private boolean active;
}
