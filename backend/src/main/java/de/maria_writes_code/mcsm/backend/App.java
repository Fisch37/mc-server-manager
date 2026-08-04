package de.maria_writes_code.mcsm.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication(scanBasePackages = "de.maria_writes_code.mcsm.backend")
@EntityScan("de.maria_writes_code.mcsm.backend")
@EnableJpaRepositories("de.maria_writes_code.mcsm.backend")
@RestController
@OpenAPIDefinition(info = @Info(title = "MC Server Manager API"))
public class App {
    public static final Logger LOGGER = LoggerFactory.getLogger("MCSM App");

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}