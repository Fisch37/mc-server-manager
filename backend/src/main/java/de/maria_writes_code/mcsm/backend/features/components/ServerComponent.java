package de.maria_writes_code.mcsm.backend.features.components;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProvider;
import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplate;

public interface ServerComponent<T extends VersionCombo> {
    ComponentIdentifier getIdentifier();
    Collection<? extends VersionProvider> getVersionProviders();

    /**
     * Generic version of {@link ServerComponent#fetchExecutable} accepting any {@link VersionCombo},
     * at the cost of a chance of failure via {@link IllegalArgumentException}.
     * @param versions Some VersionCombo that may or may not be compatible with {@link T}.
     * @param destination
     * @throws IOException
     * @throws IllegalArgumentException if the supplied <em>versions</em> are insufficient
     *  to convert into this component's {@link T}.
     * @see ServerComponent#fetchExecutable
     */
    void fetchExecutableGeneric(VersionCombo versions, Path destination) throws IOException, IllegalArgumentException;
    /**
     * Downloads 
     * @param versions
     * @param destination
     * @throws IOException
     */
    void fetchExecutable(T versions, Path destination) throws IOException;
    void startServer(Path location, ServerTemplate template) throws IOException;
}
