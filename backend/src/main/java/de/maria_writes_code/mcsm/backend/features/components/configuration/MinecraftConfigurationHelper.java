package de.maria_writes_code.mcsm.backend.features.components.configuration;

import java.nio.file.Path;
import java.util.List;

public class MinecraftConfigurationHelper implements ServerConfigurationHelper {
    @Override
    public List<Path> getLogDirectories() {
        return List.of(
            Path.of("logs"),
            Path.of("crash-reports")
        );
    }
}
