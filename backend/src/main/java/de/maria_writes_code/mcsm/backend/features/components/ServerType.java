package de.maria_writes_code.mcsm.backend.features.components;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import de.maria_writes_code.mcsm.backend.features.components.configuration.ConfigurationDescriptor;
import de.maria_writes_code.mcsm.backend.features.components.execution_helpers.ServerExecutionHelper;
import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProvider;
import de.maria_writes_code.mcsm.backend.features.runtimes.NoSuchRuntimeException;

public interface ServerType<T extends VersionCombo> {
    ComponentIdentifier getIdentifier();
    Collection<? extends VersionProvider> getVersionProviders();

    /**
     * Generic version of {@link ServerType#fetchExecutable} accepting any {@link VersionCombo},
     * at the cost of a chance of failure via {@link IllegalArgumentException}.
     * @param versions Some VersionCombo that may or may not be compatible with {@link T}.
     * @param destination
     * @param updateReceiver
     * @throws IOException
     * @throws IllegalArgumentException if the supplied <em>versions</em> are insufficient
     *  to convert into this component's {@link T}.
     * @see ServerType#fetchExecutable
     */
    void fetchExecutableGeneric(
        VersionCombo versions,
        Path destination,
        Consumer<String> updateReceiver
    ) throws IOException, IllegalArgumentException;
    /**
     * Downloads 
     * @param versions
     * @param destination
     * @param updateReceiver
     * @throws IOException
     * @throws NoSuchRuntimeException if a required runtime does not actually exist or is not available
     * @throws IllegalStateException if a required property is not present or malformed
     */
    void fetchExecutable(
        T versions,
        Path destination,
        Consumer<String> updateReceiver
    ) throws IOException;
    
    ServerExecutionHelper getExecutionHelper();
    List<ConfigurationDescriptor<?>> getAvailableProperties();

    Map<String, String> fetchVersionProperties(T versions) throws IOException;
    Map<String, String> fetchVersionPropertiesGeneric(VersionCombo versions) throws IOException, IllegalArgumentException;
}
