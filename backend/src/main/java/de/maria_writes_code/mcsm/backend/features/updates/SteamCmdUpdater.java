package de.maria_writes_code.mcsm.backend.features.updates;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import de.maria_writes_code.mcsm.backend.AppConfig;
import de.maria_writes_code.mcsm.backend.features.components.versions.NoVersions;
import de.maria_writes_code.mcsm.backend.features.server.ServerProcess;
import de.maria_writes_code.mcsm.backend.utils.Utils;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SteamCmdUpdater implements ServerUpdater<NoVersions> {
    private static final String DEFAULT_BRANCH = "public";
    private static final UriBuilderFactory URI_FACTORY = new DefaultUriBuilderFactory();
    // https://api.steamcmd.net/v1/info/1690800
    private static final UriBuilder APP_INFO_URI = URI_FACTORY.builder()
        .scheme("https")
        .host("api.steamcmd.net")
        .pathSegment("v1", "info", "{appId}")
        ;
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final String appId;
    private final String branch;
    private final AppConfig config;

    public SteamCmdUpdater(String appId, AppConfig config) {
        this(appId, DEFAULT_BRANCH, config);
    }
    public SteamCmdUpdater(String appId, String branch, AppConfig config) {
        this.appId = appId;
        this.branch = branch;
        this.config = config;
    }

    @Override
    public boolean checkUpdateAvailable(Path location) throws IOException {
        // FIXME: All of this
        return false;
        // return !getCurrentBuildId(location).equals(getBuildIds().get(branch));
    }

    @Override
    public void update(Path destination, NoVersions versions, Consumer<String> receiver) throws IOException {
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
                appId,
                "validate",
                "+quit"
            )).start(),
            code -> { }
        );
        process.getConsoleEvent().subscribe(receiver);
        
        try {
            if (!Utils.isExitCodeOk(process.await())) {
                receiver.accept("Failed to install server");
                throw new IOException("Server creation completed abnormally");
            }
        } catch (ExecutionException e) {
            throw new IOException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
    
    private Map<String, String> getBuildIds() throws IOException {
        JsonNode response;
        try (var stream = new BufferedInputStream(APP_INFO_URI.build(appId).toURL().openStream())) {
            response = MAPPER.readTree(stream);
        }
        return response.get("data")
            .get(appId)
            .get("depots")
            .get("branches")
            .propertyStream()
            .collect(Collectors.toMap(Map.Entry::getKey, entry ->
                entry.getValue().get("buildid").asText()
            ));
    }

    private String getCurrentBuildId(Path location) {
        // Using long and slicing because we need unsigned 32-bit integers.
        var appIdBytes = Arrays.copyOfRange(
            ByteBuffer.allocate(Long.BYTES)
                .order(ByteOrder.LITTLE_ENDIAN)
                .putLong(Long.parseLong(appId))
                .array(),
            0,
            4
        );
        
        throw new NotImplementedException();
    }
}
