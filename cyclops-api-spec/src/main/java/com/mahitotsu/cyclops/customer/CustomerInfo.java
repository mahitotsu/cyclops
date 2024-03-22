package com.mahitotsu.cyclops.customer;

import jakarta.validation.Valid;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@SuperBuilder(toBuilder = true)
@FieldDefaults(makeFinal = true)
public class CustomerInfo {
    @Valid
    private CustomerId id;
    private boolean active;
}
