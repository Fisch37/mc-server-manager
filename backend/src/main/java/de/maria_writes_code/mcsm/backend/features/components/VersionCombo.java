package de.maria_writes_code.mcsm.backend.features.components;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a combination of versions that, for a given component,
 * are some possible identifier for an executable.
 */
public interface VersionCombo {
    Map<String, String> getVersions();
    default Optional<String> getVersion(String versionSourceId) {
        return Optional.ofNullable(getVersions().get(versionSourceId));
    }

    public static class Mapped implements VersionCombo {
        private final Map<String, String> versions;

        public Mapped(Map<String, String> versions) {
            this.versions = versions;
        }

        @Override
        public Map<String, String> getVersions() {
            return Collections.unmodifiableMap(versions);
        }

        @Override
        public Optional<String> getVersion(String versionSourceId) {
            return Optional.ofNullable(versions.get(versionSourceId));
        }
    }
}
