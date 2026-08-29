package de.maria_writes_code.mcsm.backend;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class AppConfig {
    private static final String STEAM_CMD_ENV = "MCSM_STEAMCMD";
    private static final String STEAM_CMD_DEFAULT = "steamcmd";

    private static final String DATA_LOC_ENV = "MCSM_DATA_PATH";
    private static final File DATA_LOC_DEFAULT = Path.of("/", "var", "mcsm").toFile();

    @Autowired
    Environment env;

    public String getSteamCmd() {
        return env.getProperty(STEAM_CMD_ENV, STEAM_CMD_DEFAULT);
    }

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

    public Path getVanillaJarLocation() {
        return getDataLocation().resolve("vanilla-jars");
    }

    public Path getDatabaseLocation() {
        return getDataLocation().resolve("database.sqlite");
    }

    public void setup() throws IOException {
        for (var dir : List.of(
            getServerLocation(),
            getTemplateLocation(),
            getRuntimeLocation(),
            getVanillaJarLocation()
        )) {
            Files.createDirectories(dir);
        }
    }
}
