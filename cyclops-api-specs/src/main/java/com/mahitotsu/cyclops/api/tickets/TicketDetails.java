package com.mahitotsu.cyclops.api.tickets;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TicketDetails {

    @NotNull
    @Valid
    private TicketId ticketId;

    @NotNull
    @NotBlank
    private String title;

    private String description;

    private boolean open;
}
