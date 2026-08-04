package de.maria_writes_code.mcsm.backend.api;

import static de.maria_writes_code.mcsm.backend.api.EndpointConsts.NO_SERVER_EXISTS;

import java.io.IOException;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.ServerManager;
import de.maria_writes_code.mcsm.backend.features.server.ServerStatus;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("servers")
public class ServerExecutionEndpoints {
    @Autowired
    private ServerManager servers;

    @GetMapping("{id}/status")
    public ServerStatusObject getStatus(@PathVariable UUID id) {
        return new ServerStatusObject(getServer(id));
    }

    @GetMapping("{id}/status/follow")
    public void getStatusWS(@PathVariable UUID id) {
        throw new NotImplementedException();
    }

    @PostMapping("{id}/start")
    public void startServer(
        @PathVariable UUID id,
        @RequestParam boolean follow
    ) throws IOException {
        try {
            getServer(id).start();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Server is already running",
                e
            );
        }
        if (follow) {
            throw new NotImplementedException();
        }
    }

    @PostMapping("{id}/stop")
    public void stopServer(@PathVariable UUID id) throws IOException {
        try {
            getServer(id).stop();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Server is not running",
                e
            );
        }
    }

    @PostMapping("{id}/restart")
    public void restartServer(@PathVariable UUID id) throws IOException {
        try {
            getServer(id).restart();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Server is not running",
                e
            );
        }
    }

    @GetMapping("{id}/console")
    public void getConsoleWS(@PathVariable UUID id) {
        throw new NotImplementedException();
    }

    @PostMapping("{id}/console")
    public void sendConsoleLine(
        @PathVariable UUID id,
        @RequestBody String line
    ) throws IOException {
        getServer(id).sendCommand(line);
    }

    private ActiveServer getServer(UUID id) throws ResponseStatusException {
        return servers.get(id)
            .orElseThrow(NO_SERVER_EXISTS);
    }

    public record ServerStatusObject(UUID server_id, ServerStatus status) {
        public ServerStatusObject(ActiveServer server) {
            this(server.getId(), server.getStatus());
        }
    }
}
