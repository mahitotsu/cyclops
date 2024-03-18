package com.mahitotsu.cyclops.domain;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class DomainInfraTestConfig {

    @Bean
    @ServiceConnection("postgres")
    public PostgreSQLContainer<?> postgreSQLContainer() {

        final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
                DockerImageName.parse("public.ecr.aws/docker/library/postgres:16.2-bullseye")
                        .asCompatibleSubstituteFor("postgres"));
        return postgres;
    }
}
