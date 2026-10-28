package com.example.demo_jdbc_samples.config;

import java.sql.Connection;
import javax.sql.DataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;

@Slf4j
@Configuration
public class DataJdbcConfig {

    // Logs the database actually connected to
    @Bean
    public ApplicationRunner logDatabase(DataSource dataSource) {
        return args -> {
            try (Connection connection = dataSource.getConnection()) {
                log.info("Database: {} {} ({})", connection.getMetaData().getDatabaseProductName(),
                    connection.getMetaData().getDatabaseProductVersion(), connection.getMetaData().getURL());
            }
        };
    }

    /**
     * Unquoted table and column names, as in schema/*.sql: Oracle folds them to upper case,
     * so a quoted "customers" would not be found. Set before the mapping context reads the
     * entities.
     */
    @Bean
    public static BeanPostProcessor unquotedIdentifiers() {
        return new BeanPostProcessor() {
            @Override
            public Object postProcessBeforeInitialization(Object bean, String beanName) {
                if (bean instanceof JdbcMappingContext context) {
                    context.setForceQuote(false);
                }
                return bean;
            }
        };
    }
}
