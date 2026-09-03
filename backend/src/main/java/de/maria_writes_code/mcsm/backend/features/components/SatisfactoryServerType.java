package de.maria_writes_code.mcsm.backend.features.components;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import org.apache.commons.lang3.function.TriFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.maria_writes_code.mcsm.backend.AppConfig;
import de.maria_writes_code.mcsm.backend.features.components.execution_helpers.ServerExecutionHelper;
import de.maria_writes_code.mcsm.backend.features.components.configuration.ConfigurationDescriptor;
import de.maria_writes_code.mcsm.backend.features.components.configuration.SatisfactoryConfigurationHelper;
import de.maria_writes_code.mcsm.backend.features.components.configuration.ServerConfigurationHelper;
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

    private final static String 
        CONF_PORT = "satisfactory-port",
        CONF_MSG_PORT = "satisfactory-messaging-port",
        CONF_MSG_PORT_EXT = "satisfactory-messaging-port-external"
        ;
    @Override
    public ServerExecutionHelper getExecutionHelper() {
        return new NativeExecutionHelper(
            Path.of("FactoryServer.sh"),
            Map.of(
                // Injecting a custom home to trick satisfactory into storing its savefiles in the server directory
                "HOME", (location, properties) -> location.toAbsolutePath().toString()
            )
        ) {
            @Override
            protected List<String> getStandardArguments(Map<String, String> properties) {
                // -Port="$SERVERGAMEPORT" -ReliablePort="$SERVERMESSAGINGPORT" -ExternalReliablePort="$SERVERMESSAGINGPORT"
                var args = new ArrayList<String>(3);
                var serverPort = properties.get(CONF_PORT);
                var messagingPort = properties.get(CONF_MSG_PORT);
                var messagingPortExternal = properties.get(CONF_MSG_PORT_EXT);
                if (serverPort != null)
                    args.add("-Port=" + serverPort);
                if (messagingPort != null)
                    args.add("-ReliablePort=" + messagingPort);
                if (messagingPortExternal != null)
                    args.add("-ExternalReliablePort=" + messagingPortExternal);
                return args;
            }
        };
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
        TriFunction<String, String, String, ConfigurationDescriptor.Number> portNumber = (
            id, name, description
        ) -> new ConfigurationDescriptor.Number(
            id,
            name,
            "",
            description,
            false,
            null,
            1d,
            65_535d
        );
        return List.of(
            portNumber.apply(CONF_PORT, "Port", ""),
            portNumber.apply(CONF_MSG_PORT, "Messaging Port", ""),
            portNumber.apply(CONF_MSG_PORT_EXT, "External Messaging Port", "")
        );
    }

    @Override
    public ServerConfigurationHelper getConfigurationHelper() {
        return new SatisfactoryConfigurationHelper();
    }
}
