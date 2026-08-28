package de.maria_writes_code.mcsm.backend.features.components;

import static de.maria_writes_code.mcsm.backend.App.LOGGER;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProvider;
import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProviderCollection;

@Service
public class ComponentRegistry implements InitializingBean, VersionProviderCollection {
    private final Map<ComponentIdentifier, ServerType<?>> components;
    private final Map<String, VersionProvider> versionProviders = new HashMap<>();

    @Autowired
    public ComponentRegistry(List<ServerType<?>> components) {
        this.components = components.stream()
            .collect(Collectors.toMap(
                c -> c.getIdentifier(),
                Function.identity()
            ));
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        for (var c : components.values()) {
            LOGGER.debug("Found server type {}", c);
            for (var versionProvider : c.getVersionProviders()) {
                var oldProvider = versionProviders.put(versionProvider.getSourceIdentifier(), versionProvider);
                if (oldProvider != null && versionProvider != oldProvider) {
                    LOGGER.warn(
                        "Duplicate source identifiers. Version Provider {} replaces {} for id {}",
                        versionProvider, oldProvider, versionProvider.getSourceIdentifier()
                    );
                }
            }
        }
    }

    public ServerType<?> getComponent(ComponentIdentifier identifier) {
        return components.get(identifier);
    }

    public Collection<VersionProvider> getVersionProviders() {
        return versionProviders.values();
    }

    public @Nullable VersionProvider getVersionProvider(String sourceIdentifier) {
        return versionProviders.get(sourceIdentifier);
    }
}
