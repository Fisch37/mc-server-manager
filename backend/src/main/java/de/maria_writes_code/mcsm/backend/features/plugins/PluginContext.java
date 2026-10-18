package de.maria_writes_code.mcsm.backend.features.plugins;

public interface PluginContext {
    ServerTypeRegistry getServerTypeRegistry();
    Config getAppConfig();
}
