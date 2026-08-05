package de.maria_writes_code.mcsm.backend.api.websockets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.UUID;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;

public class ConsoleSocket extends TextWebSocketHandler implements Consumer<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleSocket.class);
    private static final ObjectMapper CONSOLE_MAPPER = new ObjectMapper();

    private final ActiveServer server;
    private final LinkedHashSet<WebSocketSession> sessions;

    public ConsoleSocket(ActiveServer server) {
        this.server = server;
        var process = server.getProcess();
        if (process == null) {
            throw new IllegalStateException("Cannot open a socket on an empty server");
        }
        process.getConsoleEvent().subscribe(this);
        sessions = new LinkedHashSet<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        sessions.add(session);

        // TODO: Send current console state
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        LOGGER.error("Transport error on websocket", exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        super.afterConnectionClosed(session, closeStatus);
        sessions.remove(session);
    }

    @Override
    public boolean supportsPartialMessages() {
        // no handling of messages occurs
        // so we don't care how large messages are received
        return true;
    }

    @Override
    public void accept(@Nullable String newLine) {
        if (newLine == null) {
            var sessionIter = new ArrayList<>(sessions);
            for (var session : sessionIter) {
                try {
                    session.close(CloseStatus.NORMAL);
                } catch (IOException e) {
                    LOGGER.error("I/O error while closing a session", e);
                }
            }
            return;
        }
        TextMessage msg;
        try {
            msg = new TextMessage(
                CONSOLE_MAPPER.writeValueAsString(
                    new ConsoleLineObject(server.getId(), newLine)
                )
            );
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize console line object", e);
            return;
        }
        sessions.forEach(session -> {
            try {
                session.sendMessage(msg);
            } catch (IOException e) {
                LOGGER.error("I/O error while sending console line", e);
            }
        });
    }

    private record ConsoleLineObject(UUID server_id, String line) { }
}
