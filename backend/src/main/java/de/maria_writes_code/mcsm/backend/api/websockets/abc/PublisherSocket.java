package de.maria_writes_code.mcsm.backend.api.websockets.abc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public abstract class PublisherSocket extends TextWebSocketHandler {
    protected static final Logger LOGGER = LoggerFactory.getLogger(PublisherSocket.class);
    private LinkedHashSet<WebSocketSession> sessions;

    public PublisherSocket() {
        sessions = new LinkedHashSet<>();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        if (!sessions.remove(session)) {
            LOGGER.warn("Tried to pop a websocket, but it wasn't present");
        }
        super.afterConnectionClosed(session, status);
    }

    protected void publish(WebSocketMessage<?> message) throws IOExceptionGroup {
        runForAll(s -> s.sendMessage(message));
    }

    protected void close(CloseStatus status) throws IOExceptionGroup {
        runForAll(s -> s.close(status));
    }

    protected void close() throws IOExceptionGroup {
        runForAll(s -> s.close());
    }

    protected Collection<WebSocketSession> getSessions() {
        return sessions;
    }

    private void runForAll(WSRunner runner) throws IOExceptionGroup {
        var exceptions = new ArrayList<IOException>(0);
        sessions.forEach(s -> {
            try {
                runner.run(s);
            } catch (IOException e) {
                exceptions.add(e);
            }
        });
        new IOExceptionGroup(exceptions).throwIfNotEmpty();
    }

    @FunctionalInterface
    private static interface WSRunner {
        void run(WebSocketSession session) throws IOException;
    }

    protected static class IOExceptionGroup extends Throwable {
        private final Collection<IOException> exceptions;

        public IOExceptionGroup(Collection<IOException> exceptions) {
            this.exceptions = exceptions;
        }

        public Collection<IOException> getExceptions() {
            return exceptions;
        }

        public void throwIfNotEmpty() throws IOExceptionGroup {
            if (!exceptions.isEmpty()) {
                throw this;
            }
        }

        public String toString() {
            return "IOExceptionGroup" + exceptions.toString();
        }
    }
}
