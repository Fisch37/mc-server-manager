package de.maria_writes_code.mcsm.backend.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import de.maria_writes_code.mcsm.backend.App;
import de.maria_writes_code.mcsm.backend.api.websockets.ListenerSocket;
import de.maria_writes_code.mcsm.backend.api.websockets.abc.PublisherSocket.IOExceptionGroup;

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

    public static String startTaskWithListeningSocket(
        WebsocketGateway gateway,
        LoggedTask task
    ) {
        var ws = new ListenerSocket();
        new Thread(() -> {
            try {
                task.run(ws);
                ws.accept(null);
            } catch (Exception e) {
                try (var stringWriter = new StringWriter(); var printWriter = new PrintWriter(stringWriter)) {
                    e.printStackTrace(printWriter);
                    ws.accept(stringWriter.toString());
                } catch (IOException ioe) {
                    ws.accept("I/O error occurred trying to print stack trace of another error. Error is " + e.getMessage());
                    ioe.printStackTrace();
                }
                // close socket
                try {
                    ws.close(CloseStatus.SERVER_ERROR);
                } catch (IOExceptionGroup e2) {
                    App.LOGGER.error("One or more sockets failed to close at server error", e2);
                }
            }
        }).start();
        return gateway.register(ws);
    }

    @FunctionalInterface 
    public static interface LoggedTask {
        void run(Consumer<String> receiver) throws Exception;
    }

    private final class Seal extends EndpointUtils { }
}
