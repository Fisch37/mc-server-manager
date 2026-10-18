package de.maria_writes_code.mcsm.backend.features.components.versions;

import java.util.Map;

public class NoVersions implements VersionCombo {
    public NoVersions() { }
    public NoVersions(VersionCombo versions) { }

    @Override
    public Map<String, String> getVersions() {
        return Map.of();
    }
    
}
