package de.maria_writes_code.mcsm.backend.features.versions;

import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

@Component
public interface VersionProvider {
    static @Nullable VersionProvider getProvider(VersionSource identifier) {
        throw new NotImplementedException();
    }

    VersionSource getIdentifier();
    Version getVersionInfo(String versionId);
    Collection<? extends Version> getVersions();
}
