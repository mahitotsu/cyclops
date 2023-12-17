package com.mahitotsu.cyclops.webapp;

import org.springframework.boot.SpringApplication;

public class TestMain {
    public static void main(final String... args) {
        SpringApplication.from(Main::main).with(TestContainerConfiguration.class).run(args);
    }
}