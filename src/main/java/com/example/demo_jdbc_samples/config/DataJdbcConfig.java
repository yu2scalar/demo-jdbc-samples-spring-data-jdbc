package com.example.demo_jdbc_samples.config;

import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jdbc.core.mapping.JdbcMappingContext;

@Configuration
public class DataJdbcConfig {

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
