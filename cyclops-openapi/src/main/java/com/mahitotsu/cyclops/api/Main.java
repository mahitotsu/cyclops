package com.mahitotsu.cyclops.api;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Main {

    public static void main(final String... args) {
        new SpringApplicationBuilder(Main.class).build(args).run(args);
    }
}