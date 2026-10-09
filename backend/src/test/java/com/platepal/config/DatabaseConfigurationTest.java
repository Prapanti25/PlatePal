package com.platepal.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.zaxxer.hikari.HikariDataSource;

class DatabaseConfigurationTest {
    @Test
    void convertsRenderPostgresUrlToJdbcUrl() {
        try (HikariDataSource dataSource = (HikariDataSource) new DatabaseConfiguration().dataSource(
                "postgresql://platepal:dev%2Bpassword@db.example:5432/platepal?sslmode=require", "", "")) {
            assertEquals("jdbc:postgresql://db.example:5432/platepal?sslmode=require", dataSource.getJdbcUrl());
            assertEquals("platepal", dataSource.getUsername());
            assertEquals("dev+password", dataSource.getPassword());
        }
    }
}