package com.mahitotsu.cyclopse.webapp.entity;

import java.util.UUID;

import org.springframework.data.annotation.Id;

import lombok.Data;

@Data
public class TicketEntity {
    @Id
    private UUID id;
    private String title;
}
