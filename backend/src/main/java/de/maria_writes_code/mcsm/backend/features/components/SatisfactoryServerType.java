package de.maria_writes_code.mcsm.backend.features.components;

import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.maria_writes_code.mcsm.backend.AppConfig;
import de.maria_writes_code.mcsm.backend.features.components.execution_helpers.ServerExecutionHelper;
import de.maria_writes_code.mcsm.backend.features.components.execution_helpers.NativeExecutionHelper;
import de.maria_writes_code.mcsm.backend.features.components.versions.NoVersions;
import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProvider;

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
    public void fetchExecutableGeneric(VersionCombo versions, Path destination)
            throws IOException, IllegalArgumentException {
        fetchExecutable(new NoVersions(versions), destination);
    }

    @Override
    // TODO: May be sensible to expand the throws clause here.
    public void fetchExecutable(NoVersions versions, Path destination) throws IOException {
        // Satisfactory downloads are done by SteamCMD at runtime.
        // steamcmd +force_install_dir ~/SatisfactoryDedicatedServer +login anonymous +app_update 1690800 validate +quit
        try {
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
            )).redirectError(Redirect.INHERIT).redirectOutput(Redirect.INHERIT).start().onExit().get();
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
    
}
