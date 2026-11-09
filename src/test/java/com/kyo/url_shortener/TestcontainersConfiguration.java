package com.kyo.url_shortener;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.DynamicPropertyRegistrar;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:17-alpine"));
    }
    @Bean
    @ServiceConnection(name = "redis")
    GenericContainer<?> redisContainer() {
        return new GenericContainer<>(DockerImageName.parse("redis:7-alpine")).withExposedPorts(6379);
    }
    @Bean
    GenericContainer<?> elasticMqContainer() {
        return new GenericContainer<>(DockerImageName.parse("softwaremill/elasticmq-native"))
                .withExposedPorts(9324);
    }

    @Bean
    DynamicPropertyRegistrar sqsProperties(GenericContainer<?> elasticMqContainer) {
        return registry -> registry.add("spring.cloud.aws.sqs.endpoint",
                () -> "http://" + elasticMqContainer.getHost() + ":" + elasticMqContainer.getMappedPort(9324));
    }
}