package com.github.tddiaz.wallet;

import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestDatasourceContextInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final PostgreSQLContainer dbContainer = new PostgreSQLContainer("postgres:15.2");

    static {
        dbContainer.start();
    }

    /**
     * Set datasource values for autoconfiguration
     */
    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        TestPropertyValues.of(
            "spring.datasource.url=" + dbContainer.getJdbcUrl(),
            "spring.datasource.username=" + dbContainer.getUsername(),
            "spring.datasource.password=" + dbContainer.getPassword(),
            "spring.datasource.driver-class-name=" + dbContainer.getDriverClassName()
        ).applyTo(applicationContext.getEnvironment());
    }
}