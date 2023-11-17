package com.mahitotsu.cyclops.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.ApplicationPidFileWriter;

@SpringBootApplication
public class Main {
    public static void main(final String... args) {
        final SpringApplication app = new SpringApplicationBuilder(Main.class).build(args);
        app.addListeners(new ApplicationPidFileWriter());
        app.run(args);
    }
}
