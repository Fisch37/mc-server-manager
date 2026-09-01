package de.maria_writes_code.mcsm.backend.features.components.configuration;

import java.nio.file.Path;
import java.util.List;

public interface ServerConfigurationHelper {
    List<Path> getLogDirectories();
}
