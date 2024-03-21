package com.mahitotsu.cyclops.customer.api;

import jakarta.validation.Valid;
import lombok.Data;

@Data
public class CustomerInfo {
    @Valid
    private CustomerId id;
    private boolean active;
}
