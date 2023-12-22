package com.mahitotsu.cyclops.webapp;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainerConfiguration {

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    public PostgreSQLContainer<?> postgresqlContainer() {
        return new PostgreSQLContainer<>(
                DockerImageName.parse("public.ecr.aws/docker/library/postgres:15.3")
                        .asCompatibleSubstituteFor("postgres"))
                .withInitScript("init-pg.sql")
                .withReuse(false);
    }
}
