package com.mahitotsu.cyclops.webapi;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

@SpringBootApplication
public class WebApi extends SpringBootServletInitializer {

    public static void main(final String... args) {
        new SpringApplicationBuilder(WebApi.class).run(args);
    }

    @Override
    protected SpringApplicationBuilder createSpringApplicationBuilder() {
        return new SpringApplicationBuilder(WebApi.class);
    }
}