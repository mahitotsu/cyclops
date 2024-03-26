package com.mahitotsu.cyclops.api.tickets;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@Value
@SuperBuilder
@Jacksonized
public class TicketId {

    @NotNull
    @Size(min = 8, max = 8)
    @Pattern(regexp = "[0-9]+")
    private final String value;
}
