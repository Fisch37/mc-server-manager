package de.maria_writes_code.mcsm.backend.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.maria_writes_code.mcsm.backend.features.components.versions.NoVersions;
import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.ServerManager;
import de.maria_writes_code.mcsm.backend.features.updates.ServerUpdater;
import io.swagger.v3.oas.annotations.parameters.RequestBody;

import static de.maria_writes_code.mcsm.backend.api.EndpointUtils.NO_SERVER_EXISTS;

@RestController
@RequestMapping("server/{id}/updates")
public class ServerUpdateEndpoints {
    private static final Supplier<ResponseStatusException> NO_UPDATES_AVAILABLE = () -> new ResponseStatusException(HttpStatus.GONE, "no updater is available");

    @Autowired
    private ServerManager servers;
    @Autowired 
    private WebsocketGateway gateway;

    @GetMapping 
    private boolean checkForUpdate(@PathVariable UUID id) throws ResponseStatusException, IOException {
        var server = servers.get(id).orElseThrow(NO_SERVER_EXISTS);
        var updater = getUpdater(server);
        return updater.checkUpdateAvailable(server.getLocation());
    }

    @PutMapping(value = "start", produces = "text/plain")
    private String performUpdate(
        @PathVariable UUID id,
        @RequestBody @Nullable HashMap<String, String> versions
    ) throws ResponseStatusException {
        var server = servers.get(id).orElseThrow(NO_SERVER_EXISTS);
        // TODO: Implement verisons
        var updater = (ServerUpdater<NoVersions>)getUpdater(server);
        return EndpointUtils.startTaskWithListeningSocket(
            gateway,
            ws -> updater.update(server.getLocation(), new NoVersions(), ws)
        );
    }

    private ServerUpdater<?> getUpdater(UUID id) throws ResponseStatusException {
        return getUpdater(servers.get(id).orElseThrow(NO_SERVER_EXISTS));
    }
    private ServerUpdater<?> getUpdater(ActiveServer server) throws ResponseStatusException {
        return server.getTemplate()
            .getServerType()
            .getUpdater()
            .orElseThrow(NO_UPDATES_AVAILABLE);
    }
}
