package com.mahitotsu.cyclops.webapp;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class Main extends SpringBootServletInitializer {
    public static void main(final String... args) {
        new SpringApplicationBuilder(Main.class).run(args);
    }

    @Override
    protected SpringApplicationBuilder createSpringApplicationBuilder() {
        return new SpringApplicationBuilder(Main.class);
    }
}