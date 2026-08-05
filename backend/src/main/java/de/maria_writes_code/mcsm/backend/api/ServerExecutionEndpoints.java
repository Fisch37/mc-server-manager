package de.maria_writes_code.mcsm.backend.api;

import static de.maria_writes_code.mcsm.backend.api.EndpointConsts.NO_SERVER_EXISTS;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import de.maria_writes_code.mcsm.backend.api.websockets.ConsoleSocket;
import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.ServerManager;
import de.maria_writes_code.mcsm.backend.features.server.ServerStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
        @RequestParam(defaultValue = "false") boolean follow
    ) throws IOException {
        var server = getServer(id);
        try {
            server.start();
        } catch (IllegalStateException e) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Server is already running",
                e
            );
        }
        if (follow) {
            performConsoleHandshake(server);
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
    public void getConsoleWS(
        @PathVariable UUID id,
        HttpServletRequest request,
        HttpServletResponse response
    ) {
        performConsoleHandshake(getServer(id));
    }

    @PostMapping(value = "{id}/console", consumes = { MediaType.TEXT_PLAIN_VALUE })
    public void sendConsoleLine(
        @PathVariable UUID id,
        @RequestBody String line
    ) throws IOException {
        assertRunningServer(id).sendCommand(line);
    }

    private void performConsoleHandshake(
        ActiveServer server
    ) throws ResponseStatusException {
        var reqAttrs = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        var request = reqAttrs.getRequest();
        var response = reqAttrs.getResponse();
        assertRunningServer(server);
        new DefaultHandshakeHandler().doHandshake(
            new ServletServerHttpRequest(request),
            new ServletServerHttpResponse(response),
            new ConsoleSocket(server),
            Map.of()
        );
    }

    private ActiveServer getServer(UUID id) throws ResponseStatusException {
        return servers.get(id)
            .orElseThrow(NO_SERVER_EXISTS);
    }

    private ActiveServer assertRunningServer(UUID id) throws ResponseStatusException {
        var server = getServer(id);
        assertRunningServer(server);
        return server;
    }

    private void assertRunningServer(ActiveServer server) throws ResponseStatusException {
        if (!server.getStatus().isAlive()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Server must be running, starting, or stopping");
        }
    }

    public record ServerStatusObject(UUID server_id, ServerStatus status) {
        public ServerStatusObject(ActiveServer server) {
            this(server.getId(), server.getStatus());
        }
    }
}
