package com.mahitotsu.cyclops.api.tickets;

import java.util.Collections;
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
    default List<@NotNull @Valid TicketDetails> listTickets() {
        return Collections.emptyList();
    }

    @PostExchange(url = "/describe-ticket")
    @Valid
    default TicketDetails describeTicket(@NotNull @Valid TicketId ticketId) {
        return null;
    }

    @PostExchange(url = "/create-ticket")
    @Valid
    default TicketId createTicket(@NotBlank String title, @NotNull String description) {
        return null;

    }

    @PostExchange(url = "/update-ticket")
    default boolean updaeteTicket(@NotNull @Valid TicketId ticketId, @NotNull String description) {
        return false;
    }

    @PostExchange(url = "/close-ticket")
    default boolean closeTicket(@NotNull @Valid TicketId ticketId) {
        return false;
    }
}
