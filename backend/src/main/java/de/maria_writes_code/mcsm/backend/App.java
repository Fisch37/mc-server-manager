package de.maria_writes_code.mcsm.backend;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@SpringBootApplication(scanBasePackages = "de.maria_writes_code.mcsm.backend")
@EntityScan("de.maria_writes_code.mcsm.backend")
@EnableJpaRepositories(value = "de.maria_writes_code.mcsm.backend", considerNestedRepositories = true)
@RestController
@OpenAPIDefinition(
    info = @Info(title = "MC Server Manager API"),
    servers = {
        @Server(url = "/", description = "Develop"),
        @Server(url = "/api", description = "Production Docker")
    }
)
public class App implements InitializingBean {
    public static final Logger LOGGER = LoggerFactory.getLogger("MCSM App");

    @Autowired
    private AppConfig config;

    public static void main(String[] args) {
        // try {
        //     new de.maria_writes_code.mcsm.backend.features.runtimes.AdoptiumRuntime(
        //         25,
        //         java.nio.file.Path.of("/home/maria/Documents/mc-server-manager/run/runtimes")
        //     );
        // } catch (java.io.IOException e) {
        //     e.printStackTrace();
        //     return;
        // }
        SpringApplication.run(App.class, args);
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        config.setup();
    }
}