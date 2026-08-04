package de.maria_writes_code.mcsm.backend;

import java.io.File;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class CustomAppConfig {
    private static final String DATA_LOC_ENV = "MCSM_DATA_PATH";
    private static final File DATA_LOC_DEFAULT = Path.of("/", "var", "mcsm").toFile();

    @Autowired
    Environment env;

    private Path getDataLocation() {
        // Hacky forwards-backwards conversion, because Path is actually closer to what we want
        // (but env cannot convert Strings to Paths [I think bc Path is OS independent])
        return env.getProperty(DATA_LOC_ENV, File.class, DATA_LOC_DEFAULT).toPath();
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
