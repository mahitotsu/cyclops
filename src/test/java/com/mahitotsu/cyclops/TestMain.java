package com.mahitotsu.cyclops;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.context.annotation.Import;

import com.mahitotsu.cyclops.domain.DomainInfraTestConfig;

@SpringBootConfiguration
@Import({ DomainInfraTestConfig.class })
public class TestMain {

    public static void main(final String... args) {
        SpringApplication.from(Main::main).run(args);
    }
}
