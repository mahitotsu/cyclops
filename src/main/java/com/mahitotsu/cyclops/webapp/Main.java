package com.mahitotsu.cyclops.webapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication
public class Main {
   
    public static void main(final String ...args) {
        final SpringApplication app = new SpringApplicationBuilder(Main.class).build(args);
        app.run(args);
    }
}
