package de.maria_writes_code.mcsm.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@SpringBootApplication
@RestController
@OpenAPIDefinition(info = @Info(title = "MC Server Manager API"))
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}