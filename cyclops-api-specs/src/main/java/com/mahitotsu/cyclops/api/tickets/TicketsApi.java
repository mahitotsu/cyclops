package com.mahitotsu.cyclops.api.tickets;

import java.util.List;

import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@HttpExchange(url = "/api/tickets")
public interface TicketsApi {

    @PostExchange(url = "/list-tickets")
    @NotNull
    List<@NotNull @Valid TicketDetails> listTickets();

    @PostExchange(url = "/describe-ticket")
    @Valid
    TicketDetails describeTicket(@NotNull @Valid TicketId ticketId);

    @PostExchange(url = "/create-ticket")
    @Valid
    TicketId createTicket(@NotBlank String title, @NotNull String description);

    @PostExchange(url = "/update-ticket")
    boolean updaeteTicket(@NotNull @Valid TicketId ticketId, @NotNull String description);

    @PostExchange(url = "/close-ticket")
    boolean closeTicket(@NotNull @Valid TicketId ticketId);
}
