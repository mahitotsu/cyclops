package com.mahitotsu.cyclops.customer;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Value;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Value
@Jacksonized
@SuperBuilder(toBuilder = true)
@FieldDefaults(makeFinal = true)
public class CustomerId {
    @NotNull
    @Pattern(regexp = "[0-9]{8}")
    private String value;
}
