package de.maria_writes_code.mcsm.backend.features.plugins;

import java.nio.file.Path;

public interface Config {
    String getSteamCmd();
    Path getDataLocation();
    
    Path getServerLocation();
    Path getTemplateLocation();
    Path getRuntimeLocation();
    Path getPluginsLocation();
}
