package de.maria_writes_code.mcsm.backend.features.plugins;

import de.maria_writes_code.mcsm.backend.features.components.ServerType;
import de.maria_writes_code.mcsm.backend.features.components.versions.VersionCombo;

public interface PluginServerType<T extends VersionCombo> extends ServerType<T> {
    default void onInitialized(PluginContext context) throws Exception { }
}
