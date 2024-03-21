package com.mahitotsu.cyclops.customer.api;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CustomerId {
    @NotNull
    @Pattern(regexp = "[0-9]{8}")
    private String value;
}
