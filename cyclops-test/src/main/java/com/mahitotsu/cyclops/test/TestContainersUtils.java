package com.mahitotsu.cyclops.test;

import org.slf4j.LoggerFactory;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TestContainersUtils {

    public static PostgreSQLContainer<?> postgreSQLContainer() {
        final PostgreSQLContainer<?> postgresql = new PostgreSQLContainer<>(DockerImageName
                .parse("public.ecr.aws/docker/library/postgres:16.2-bullseye").asCompatibleSubstituteFor("postgres"));
        postgresql.withLogConsumer(new Slf4jLogConsumer(LoggerFactory.getLogger(PostgreSQLContainer.class)));
        return postgresql;
    }
}
