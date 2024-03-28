package com.mahitotsu.cyclops.api.tickets;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@RestController
public class TicketsController implements TicketsApi {

    @Override
    public @NotNull List<@NotNull @Valid TicketDetails> listTickets() {
        throw new UnsupportedOperationException("Unimplemented method 'listTickets'");
    }

    @Override
    public @Valid TicketDetails describeTicket(@NotNull @Valid final TicketId ticketId) {
        throw new UnsupportedOperationException("Unimplemented method 'describeTicket'");
    }

    @Override
    public @Valid TicketId createTicket(@NotBlank final String title, @NotNull final String description) {
        throw new UnsupportedOperationException("Unimplemented method 'createTicket'");
    }

    @Override
    public boolean updaeteTicket(@NotNull @Valid final TicketId ticketId, @NotNull final String description) {
        throw new UnsupportedOperationException("Unimplemented method 'updaeteTicket'");
    }

    @Override
    public boolean closeTicket(@NotNull @Valid final TicketId ticketId) {
        throw new UnsupportedOperationException("Unimplemented method 'closeTicket'");
    }
    
}
