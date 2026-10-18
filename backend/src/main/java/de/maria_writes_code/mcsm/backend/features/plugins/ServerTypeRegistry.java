package de.maria_writes_code.mcsm.backend.features.plugins;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import de.maria_writes_code.mcsm.backend.features.components.ComponentIdentifier;
import de.maria_writes_code.mcsm.backend.features.components.ServerType;
import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProviderCollection;

@NullMarked 
public interface ServerTypeRegistry extends VersionProviderCollection {
    @Nullable ServerType<?> getComponent(ComponentIdentifier id);
}
