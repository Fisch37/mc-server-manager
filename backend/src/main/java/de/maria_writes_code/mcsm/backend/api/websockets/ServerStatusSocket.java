package de.maria_writes_code.mcsm.backend.api.websockets;

import java.io.IOException;
import java.util.function.Consumer;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.maria_writes_code.mcsm.backend.api.ServerExecutionEndpoints;
import de.maria_writes_code.mcsm.backend.api.websockets.abc.JsonPublisherSocket;
import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.ServerStatus;

public class ServerStatusSocket extends JsonPublisherSocket implements Consumer<ServerStatus> {
    private final ActiveServer server;
    @SuppressWarnings("unused")
    private final Thread periodicUpdatesThread;

    public ServerStatusSocket(ActiveServer server) {
        this.server = server;
        server.getStatusObserver().subscribe(this);
        periodicUpdatesThread = Thread.ofVirtual()
            .name("ServerStatusSocket-" + server.getId())
            .start(this::periodicUpdates);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        try {
            session.sendMessage(new TextMessage(MAPPER.writeValueAsString(
                new ServerExecutionEndpoints.ServerStatusObject(server)
            )));
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize server status object", e);
        } catch (IOException e) {
            LOGGER.error("I/O exception when sending server status object", e);
        }
    }

    @Override
    public void accept(ServerStatus status) {
        publishWithLogging(new ServerExecutionEndpoints.ServerStatusObject(
            server.getId(), status
        ));
    }
    
    private void publishWithLogging(Object o) {
        try {
            publish(o);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize server status object", e);
        } catch (IOExceptionGroup e) {
            LOGGER.error("One or more I/O errors occured while sending server status object", e);
        }
    }

    private void periodicUpdates() {
        while (true) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break;
            }
            publishWithLogging(new ServerExecutionEndpoints.ServerStatusObject(server));
        }
    }
}
