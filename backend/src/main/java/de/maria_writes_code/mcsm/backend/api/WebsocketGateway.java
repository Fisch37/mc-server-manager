package de.maria_writes_code.mcsm.backend.api;

import static de.maria_writes_code.mcsm.backend.api.EndpointUtils.performWSHandshake;

import java.time.Duration;
import java.util.UUID;
import org.springframework.context.annotation.Scope;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.WebSocketHandler;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

@Service @Scope("singleton")
@RestController
public class WebsocketGateway {
    private static final Duration HANDLER_TTL = Duration.ofSeconds(30);
    private final Cache<String, Handler> handlers = CacheBuilder.newBuilder()
        .expireAfterWrite(HANDLER_TTL)
        .build();

    @GetMapping("gateway")
    private void gatewayHandler(@RequestParam String token) {
        var handler = handlers.getIfPresent(token);
        if (handler == null)
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No handler with that token exists");
        else {
            performWSHandshake(handler.handler);
        }
    }

    /**
     * Registers a handler with the gateway
     * @param handler The handler to register
     * @return the UUID token identifying this handler
     */
    public String register(WebSocketHandler handler) {
        UUID token;
        boolean isDuplicated;
        do {
            token = UUID.randomUUID();
            isDuplicated = handlers.asMap().putIfAbsent(token.toString(), new Handler(handler)) != null;
        } while (isDuplicated);
        return token.toString();
    }

    private record Handler(WebSocketHandler handler) { }
}
