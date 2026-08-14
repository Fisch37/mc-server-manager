package de.maria_writes_code.mcsm.backend.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import de.maria_writes_code.mcsm.backend.AppConfig;

@Configuration
public class DBConfig {
    @Autowired AppConfig config;

    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC");
        dataSource.setUrl("jdbc:sqlite:" + config.getDatabaseLocation().toAbsolutePath().toString());
        // dataSource.setUrl("jdbc:sqlite:test.sqlite"/*env.getProperty("url")*/);
        dataSource.setUsername("sa");
        dataSource.setPassword("sa");
        return dataSource;
    }
}
