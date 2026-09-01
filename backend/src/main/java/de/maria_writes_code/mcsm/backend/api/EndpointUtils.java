package de.maria_writes_code.mcsm.backend.api;

import java.util.Map;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

public abstract sealed class EndpointUtils permits EndpointUtils.Seal {
    public static final Supplier<ResponseStatusException> NO_SERVER_EXISTS = () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "no server with that id exists");

    public static void performWSHandshake(WebSocketHandler handler) {
        var reqAttrs = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        var request = reqAttrs.getRequest();
        var response = reqAttrs.getResponse();
        new DefaultHandshakeHandler().doHandshake(
            new ServletServerHttpRequest(request),
            new ServletServerHttpResponse(response),
            handler,
            Map.of()
        );
    }

    private final class Seal extends EndpointUtils { }
}
