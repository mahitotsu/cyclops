package com.mahitotsu.cyclops;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class TestRuntimeConfig {

    @Bean
    @ServiceConnection("postgres")
    public PostgreSQLContainer<?> postgreSQLContainer() {

        final PostgreSQLContainer<?> container = new PostgreSQLContainer<>(
                DockerImageName.parse("public.ecr.aws/docker/library/postgres:bullseye")
                        .asCompatibleSubstituteFor("postgres"));
        return container;
    }
}
