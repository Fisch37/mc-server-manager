package de.maria_writes_code.mcsm.backend.features.components.configuration;

import java.nio.file.Path;
import java.util.List;

public class SatisfactoryConfigurationHelper implements ServerConfigurationHelper {

    @Override
    public List<Path> getLogDirectories() {
        return List.of(
            Path.of("FactoryGame", "Saved", "Logs")
        );
    }   
}
