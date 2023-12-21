package com.mahitotsu.cyclops.webapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing()
public class Main {
    public static void main(final String... args) {
        new SpringApplicationBuilder(Main.class).run(args);
    }
}