package de.maria_writes_code.api;

import java.util.UUID;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("servers")
public class ServerExecutionEndpoints {
    @GetMapping("{id}/status")
    public ServerStatusObject getStatus(@PathVariable UUID id) {
        throw new NotImplementedException();
    }

    @GetMapping("{id}/status/follow")
    public void getStatusWS(@PathVariable UUID id) {
        throw new NotImplementedException();
    }

    @PostMapping("{id}/start")
    public void startServer(
        @PathVariable UUID id,
        @RequestParam boolean follow
    ) {
        throw new NotImplementedException();
    }

    @PostMapping("{id}/stop")
    public void stopServer(@PathVariable UUID id) {
        throw new NotImplementedException();
    }

    @PostMapping("{id}/restart")
    public void restartServer(@PathVariable UUID id) {
        throw new NotImplementedException();
    }

    @GetMapping("{id}/console")
    public void getConsoleWS(@PathVariable UUID id) {
        throw new NotImplementedException();
    }

    @PostMapping("{id}/console")
    public void sendConsoleLine(@PathVariable UUID id) {
        throw new NotImplementedException();
    }

    public record ServerStatusObject() { }
}
