package br.com.alvar.kanban;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration(proxyBeanMethods = false)
class PostgresTestConfiguration {
    @Bean
    @org.springframework.context.annotation.Primary
    java.time.Clock testClock() {
        return java.time.Clock.fixed(java.time.Instant.parse("2026-09-22T15:00:00Z"), java.time.ZoneId.of("America/Fortaleza"));
    }

    @Bean
    @ServiceConnection
    PostgreSQLContainer<?> postgres() {
        return new PostgreSQLContainer<>("postgres:17.11-alpine");
    }
}
