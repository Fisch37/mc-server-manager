package de.maria_writes_code.mcsm.backend.features.server;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service
@Scope("singleton")
public class ServerManager {
    /**
     * Remove a server from the internal storage.
     * @param server
     */
    synchronized void drop(ActiveServer server) {
        throw new NotImplementedException();
    }

    /**
     * Add a server to internal storage that was only previously removed.
     * Caller guarantees that the server is still in a valid state.
     */
    synchronized void revive(ActiveServer server) {
        throw new NotImplementedException();
    }
}
