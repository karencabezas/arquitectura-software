package com.example.productosapi.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuración de la base de datos y JPA
 */
@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.example.productosapi.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.example.productosapi.infrastructure.persistence.repository")
public class DatabaseConfig {
    // La configuración se establece mediante anotaciones y properties
}