package de.maria_writes_code.mcsm.backend;

import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CustomAppConfig {
    private static final String DATA_LOC_ENV = "MCSM_DATA_PATH";
    private static final Path DATA_LOC_DEFAULT = Path.of("var", "mcsm");

    @Autowired
    Environment env;

    Path getDataLocation() {
        return env.getProperty(DATA_LOC_ENV, Path.class, DATA_LOC_DEFAULT);
    }

    public Path getServerLocation() {
        return getDataLocation().resolve("servers");
    }
    
    public Path getTemplateLocation() {
        return getDataLocation().resolve("templates");
    }

    public Path getRuntimeLocation() {
        return getDataLocation().resolve("runtimes");
    }

    public Path getDatabaseLocation() {
        return getDataLocation().resolve("database.sqlite");
    }
}
