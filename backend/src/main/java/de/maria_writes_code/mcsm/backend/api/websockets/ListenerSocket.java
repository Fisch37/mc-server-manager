package de.maria_writes_code.mcsm.backend.api.websockets;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import com.fasterxml.jackson.core.JsonProcessingException;
import de.maria_writes_code.mcsm.backend.api.websockets.abc.JsonPublisherSocket;

public class ListenerSocket extends JsonPublisherSocket implements Consumer<String> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleSocket.class);

    protected List<String> backlog = new ArrayList<>();

    public ListenerSocket() {

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);

        sendMessage(session, new BacklogObject(backlog));
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        LOGGER.error("Transport error on websocket", exception);
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
            try {
                super.close(CloseStatus.NORMAL);
            } catch (IOExceptionGroup e) {
                LOGGER.error("One or more I/O errors occured while closing a session", e);
            }
            return;
        } else {
            backlog.add(newLine);
            try {
                publish(
                    new LineObject(newLine)
                );
            } catch (JsonProcessingException e) {
                LOGGER.error("Failed to serialize console line object", e);
            } catch (IOExceptionGroup e) {
                LOGGER.error("One or more I/O errors occurred while sending console line", e);
            }
        }
    }

    protected record LineObject(String line) { }
    protected record BacklogObject(List<String> backlog) { }
}
