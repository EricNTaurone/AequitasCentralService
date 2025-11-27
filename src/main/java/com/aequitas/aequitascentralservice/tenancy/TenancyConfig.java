package com.aequitas.aequitascentralservice.tenancy;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.aequitas.aequitascentralservice.tenancy.datasource.TenantAwareDataSource;

/**
 * Registers tenancy infrastructure beans.
 */
@Configuration
public class TenancyConfig {

    /**
     * Creates the base DataSource bean that will be wrapped.
     *
     * @param properties the DataSource properties from application configuration
     * @return the base DataSource
     */
    @Bean
    @ConfigurationProperties("spring.datasource.hikari")
    public DataSource dataSource(final DataSourceProperties properties) {
        return properties.initializeDataSourceBuilder().build();
    }

    /**
     * Wraps the auto-configured {@link DataSource} to propagate tenant session variables.
     *
     * @param delegate base data source.
     * @return tenant-aware wrapper.
     */
    @Bean
    @Primary
    public DataSource tenantAwareDataSource(@Qualifier("dataSource") final DataSource delegate) {
        return new TenantAwareDataSource(delegate);
    }
}
