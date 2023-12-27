package com.mahitotsu.cyclopse.webapp.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mahitotsu.cyclopse.webapp.service.TicketService;

@RestController
@RequestMapping(path = "/api/tickets")
public class TicketController {

    @Autowired
    private TicketService ticketService;
}
