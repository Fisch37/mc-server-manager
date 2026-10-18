package de.maria_writes_code.mcsm.backend.features.plugins;

import java.util.List;

public record PluginConfig(
    int version,
    String name,
    List<String> entrypoints,
    List<String> server_types
) { }
