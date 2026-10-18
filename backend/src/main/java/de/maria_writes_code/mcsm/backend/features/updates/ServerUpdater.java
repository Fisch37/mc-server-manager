package de.maria_writes_code.mcsm.backend.features.updates;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import de.maria_writes_code.mcsm.backend.features.components.versions.VersionCombo;

public interface ServerUpdater<T extends VersionCombo> {
    boolean checkUpdateAvailable(Path location) throws IOException;
    /**
     * 
     * @param versions The version to update to, or {@code null} to update to the most recent version.
     * @throws IOException
     */
    void update(Path destination, @Nullable T versions, Consumer<String> receiver) throws IOException;
}
