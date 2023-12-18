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
    public PostgreSQLContainer<?> postgresqlContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("public.ecr.aws/docker/library/postgres:16")
                .asCompatibleSubstituteFor("postgres")).withReuse(false);
    }
}
