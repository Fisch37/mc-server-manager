package de.maria_writes_code.mcsm.backend.features.components;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.maria_writes_code.mcsm.backend.AppConfig;
import de.maria_writes_code.mcsm.backend.features.components.execution_helpers.ServerExecutionHelper;
import de.maria_writes_code.mcsm.backend.features.components.configuration.ConfigurationDescriptor;
import de.maria_writes_code.mcsm.backend.features.components.execution_helpers.NativeExecutionHelper;
import de.maria_writes_code.mcsm.backend.features.components.versions.NoVersions;
import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProvider;
import de.maria_writes_code.mcsm.backend.features.server.ServerProcess;
import de.maria_writes_code.mcsm.backend.utils.Utils;

@Service
public class SatisfactoryServerType implements ServerType<NoVersions> {
    private static final String SATISFACTORY_SERVER_ID = "1690800";

    @Autowired
    AppConfig config;

    @Override
    public ComponentIdentifier getIdentifier() {
        return ComponentIdentifier.Satisfactory;
    }

    @Override
    public Collection<? extends VersionProvider> getVersionProviders() {
        return List.of();
    }

    @Override
    public void fetchExecutableGeneric(VersionCombo versions, Path destination, Consumer<String> updateReceiver)
            throws IOException, IllegalArgumentException {
        fetchExecutable(new NoVersions(versions), destination, updateReceiver);
    }

    @Override
    // TODO: May be sensible to expand the throws clause here.
    public void fetchExecutable(NoVersions versions, Path destination, Consumer<String> updateReceiver) throws IOException {
        // Satisfactory downloads are done by SteamCMD at runtime.
        // steamcmd +force_install_dir ~/SatisfactoryDedicatedServer +login anonymous +app_update 1690800 validate +quit
        var process = new ServerProcess(
            new ProcessBuilder(List.of(
                config.getSteamCmd(),
                "+force_install_dir",
                // absolute path is required for SteamCMD to behave safely
                destination.toAbsolutePath().toString(),
                "+login",
                "anonymous",
                "+app_update",
                SATISFACTORY_SERVER_ID,
                "validate",
                "+quit"
            )).start(),
            code -> { }
        );
        process.getConsoleEvent().subscribe(line -> updateReceiver.accept("Satisfactory: " + line));
        
        try {
            if (!Utils.isExitCodeOk(process.await())) {
                updateReceiver.accept("Satisfactory: Failed to install server");
                throw new IOException("Server creation completed abnormally");
            }
        } catch (ExecutionException e) {
            throw new IOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServerExecutionHelper getExecutionHelper() {
        return new NativeExecutionHelper(Path.of("FactoryServer.sh"));
    }

    @Override
    public Map<String, String> fetchVersionProperties(NoVersions versions) throws IOException {
        return Map.of();
    }

    @Override
    public Map<String, String> fetchVersionPropertiesGeneric(VersionCombo versions)
            throws IOException, IllegalArgumentException {
        return fetchVersionProperties(new NoVersions(versions));
    }

    @Override
    public List<ConfigurationDescriptor<?>> getAvailableProperties() {
        return List.of();
    }
}
