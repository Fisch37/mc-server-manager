package de.maria_writes_code.mcsm.backend.api.websockets;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import de.maria_writes_code.mcsm.backend.utils.Either;
import jakarta.annotation.Nullable;

public class QueueWebSocket implements WebSocketHandler {
    private WebSocketSession ws;
    private BlockingQueue<Either<WebSocketMessage<?>, ConnectionClosedException>> receiveQueue;

    /**
     * Blocks waiting for a new message to be received
     * @return The message
     */
    protected WebSocketMessage<?> takeMessage() throws InterruptedException, ConnectionClosedException {
        return unwrap(receiveQueue.take());
    }

    protected @Nullable WebSocketMessage<?> pollMessage() throws ConnectionClosedException {
        var value = receiveQueue.poll();
        if (value == null) {
            return null;
        } else {
            return unwrap(value);
        }
    }

    private static WebSocketMessage<?> unwrap(Either<WebSocketMessage<?>, ConnectionClosedException> data) throws ConnectionClosedException {
        return data.left().orElseThrow(() -> data.right().get());
    }

    /**
     * Send a message
     * @param message The message
     * @throws IOException An I/O exception occurred while sending the message
     */
    protected void sendMessage(WebSocketMessage<?> message) throws IOException {
        ws.sendMessage(message);
    }

    public static class ConnectionClosedException extends Exception {
        private CloseStatus status;

        public ConnectionClosedException(CloseStatus status) {
            this.status = status;
        }

        public CloseStatus getCloseStatus() {
            return status;
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterConnectionEstablished'");
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleMessage'");
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'handleTransportError'");
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'afterConnectionClosed'");
    }

    @Override
    public boolean supportsPartialMessages() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'supportsPartialMessages'");
    }
}
