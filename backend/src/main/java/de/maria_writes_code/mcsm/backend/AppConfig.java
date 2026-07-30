package de.maria_writes_code.mcsm.backend;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class AppConfig {
    @Autowired Environment env;

    @Bean
    public DataSource dataSource() {
        final DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.sqlite.JDBC"/*env.getProperty("driverClassName")*/);
        dataSource.setUrl("jdbc:sqlite:test.sqlite"/*env.getProperty("url")*/);
        dataSource.setUsername("sa"/*env.getProperty("user")*/);
        dataSource.setPassword("sa"/*env.getProperty("password")*/);
        return dataSource;
    }    
}
