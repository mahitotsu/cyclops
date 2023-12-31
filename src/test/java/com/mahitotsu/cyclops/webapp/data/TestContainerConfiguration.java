package com.mahitotsu.cyclops.webapp.data;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class TestContainerConfiguration {

    @Bean
    @ServiceConnection()
    @SuppressWarnings("resource")
    public PostgreSQLContainer<?> rdbContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("public.ecr.aws/docker/library/postgres:15.3")
                .asCompatibleSubstituteFor("postgres"))
                .withEnv("POSTGRES_USER", "pguser")
                .withEnv("POSTGRES_PASSWORD", "pgpass")
                .withEnv("POSTGRES_DB", "pgdb");
    }
}
