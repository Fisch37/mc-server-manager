package de.maria_writes_code.mcsm.backend.features.plugins;

public interface PluginEntrypoint {
    void onInitialized(PluginContext context) throws Exception;
}
