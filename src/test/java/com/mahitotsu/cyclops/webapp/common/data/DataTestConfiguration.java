package com.mahitotsu.cyclops.webapp.common.data;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@Configuration
public class DataTestConfiguration {

    @Bean
    @ServiceConnection
    @SuppressWarnings("resource")
    public PostgreSQLContainer<?> rdbContainer() {
        return new PostgreSQLContainer<>(
                DockerImageName.parse("public.ecr.aws/docker/library/postgres:15.3-alpine3.18")
                        .asCompatibleSubstituteFor("postgres"))
                .withUsername("pguser")
                .withPassword("pgpass")
                .withDatabaseName("pgdb");
    }
}
