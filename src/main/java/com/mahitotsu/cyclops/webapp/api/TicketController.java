package com.mahitotsu.cyclops.webapp.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/tickets")
public class TicketController {

    @GetMapping(path = "/{ticketId}")
    public String getTicket(@PathVariable final String ticketId) {
        return "get " + ticketId;
    }
}
