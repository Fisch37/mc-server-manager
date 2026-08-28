package de.maria_writes_code.mcsm.backend.features.components.versions;

import java.util.Collection;

import org.jspecify.annotations.Nullable;

public interface VersionProviderCollection {
    Collection<VersionProvider> getVersionProviders();

    @Nullable VersionProvider getVersionProvider(String sourceIdentifier);
}
