package com.mahitotsu.cyclops.api.tickets;

import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange(url = "/api/tickets")
public interface TicketsApi {

    void createTicket();

    void changeStatus();

    void updateTicket();
}
