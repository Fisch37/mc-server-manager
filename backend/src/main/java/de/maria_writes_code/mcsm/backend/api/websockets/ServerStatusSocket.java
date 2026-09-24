package de.maria_writes_code.mcsm.backend.api.websockets;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.springframework.web.socket.WebSocketSession;

import com.fasterxml.jackson.core.JsonProcessingException;

import de.maria_writes_code.mcsm.backend.api.ServerExecutionEndpoints;
import de.maria_writes_code.mcsm.backend.api.websockets.abc.JsonPublisherSocket;
import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.ServerStatus;

public class ServerStatusSocket extends JsonPublisherSocket {
    private static long NEXT_ID = 1;
    private final Collection<ActiveServer> servers;
    @SuppressWarnings("unused")
    private final Collection<Consumer<ServerStatus>> listeners;
    @SuppressWarnings("unused")
    private final Thread periodicUpdatesThread;

    public ServerStatusSocket(ActiveServer server) {
        this(List.of(server));
    }
    public ServerStatusSocket(Collection<ActiveServer> servers) {
        this.servers = servers;
        listeners = servers
            .stream()
            .map(server -> {
                Consumer<ServerStatus> listener = status -> publish(server.getId(), status);
                server.getStatusObserver().subscribe(listener);
                return listener;
            }).collect(Collectors.toList());
        periodicUpdatesThread = Thread.ofVirtual()
            .name("ServerStatusSocket-" + NEXT_ID++)
            .start(this::periodicUpdates);
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        publishAll();
    }

    protected void publish(UUID serverId, ServerStatus status) {
        publishWithLogging(new ServerExecutionEndpoints.ServerStatusObject(
            serverId, status
        ));
    }

    protected void publishAll() {
        for (var server : servers) {
            publish(server.getId(), server.getStatus());
        }
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
            publishAll();
        }
    }
}
