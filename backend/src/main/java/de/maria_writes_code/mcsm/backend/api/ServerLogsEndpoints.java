package de.maria_writes_code.mcsm.backend.api;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.ServerManager;

import static de.maria_writes_code.mcsm.backend.api.EndpointConsts.NO_SERVER_EXISTS;

@RestController
@RequestMapping("server")
public class ServerLogsEndpoints {
    @Autowired
    ServerManager servers;

    @GetMapping("{id}/logs")
    public Stream<String> getAllLogFiles(@PathVariable UUID id) throws IOException {
        return getServer(id).getLogFiles()
            .stream()
            .map(f -> f.getName());
    }

    @GetMapping(value = "{id}/logs/content", produces = "text/plain")
    public ResponseEntity<InputStreamResource> getLogFile(
        @PathVariable UUID id,
        @RequestParam("log_name") String logName
    ) throws IOException {
        var logFile = getServer(id).getLogFiles()
            .stream()
            .filter(f -> f.getName().equals(logName))
            .findAny()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No log file with name '" + logName + "' exists"));
        var fileStream = logFile.getContent();
        return new ResponseEntity<>(new InputStreamResource(fileStream.inputStream()), HttpStatus.OK);
    }

    private ActiveServer getServer(UUID id) throws ResponseStatusException {
        return servers.get(id)
            .orElseThrow(NO_SERVER_EXISTS);
    }
}
