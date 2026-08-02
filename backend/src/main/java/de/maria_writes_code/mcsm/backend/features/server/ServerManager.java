package de.maria_writes_code.mcsm.backend.features.server;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class ServerManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerManager.class);

    @Autowired
    private ServerRepository repo;

    private ConcurrentMap<UUID, ActiveServer> servers;

    public ServerManager() throws IOException {
        servers = repo.findAll()
            .stream()
            .map(ActiveServer::new)
            .collect(Collectors.toConcurrentMap(
                s -> s.getServer().getId(),
                Function.identity()
            ));
        
    }

    public Optional<ActiveServer> get(UUID uuid) {
        return Optional.ofNullable(servers.get(uuid));
    }

    public Stream<ActiveServer> stream() {
        return servers.values().stream();
    }

    /**
     * Remove a server from the internal storage.
     * @param server
     */
    void drop(ActiveServer server) {
        if (servers.remove(server.getId()) == null) {
            LOGGER.warn(
                "Tried to drop server {}, but it is not present",
                server.getId()
            );
        }
    }

    /**
     * Add a server to internal storage that was only previously removed.
     * Caller guarantees that the server is still in a valid state.
     */
    void revive(ActiveServer server) {
        if (servers.putIfAbsent(server.getId(), server) != null) {
            LOGGER.warn(
                "Tried to revive server {}, but one of its uuid exists!",
                server.getId()
            );
        }
    }
}
