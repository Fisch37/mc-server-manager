package de.maria_writes_code.mcsm.backend.features.components;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProvider;

@Service
public class VersionProviderCollection {
    private final Map<String, VersionProvider> versionProviders;

    @Autowired
    public VersionProviderCollection(List<VersionProvider> versionProviders) {
        this.versionProviders = versionProviders
            .stream()
            .collect(Collectors.toUnmodifiableMap(
                v -> v.getSourceIdentifier(),
                Function.identity()
            ));
    }

    public @Nullable VersionProvider getProvider(String sourceId) {
        return versionProviders.get(sourceId);
    }
}
