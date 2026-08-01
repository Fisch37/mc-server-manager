package de.maria_writes_code.mcsm.backend.features.server;

import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;

import de.maria_writes_code.mcsm.backend.CustomAppConfig;
import de.maria_writes_code.mcsm.backend.features.versions.Version;
import de.maria_writes_code.mcsm.backend.features.versions.VersionRegistry;

@NullMarked
public class ActiveServer {
    @Autowired
    private CustomAppConfig appConfig;
    @Autowired
    private ServerRepository repo;
    @Autowired
    private ServerManager serverManager;
    @Autowired
    private VersionRegistry versionRegistry;

    private final UUID id;
    private Server server;
    @Nullable
    private ServerProcess process;
    
    public ActiveServer(Server server) {
        this.id = server.getId();
        this.server = server;
    }

    public Server getServer() {
        return server;
    }

    public Path getLocation() {
        return appConfig.getServerLocation().resolve(id.toString());
    }

    public @Nullable Version getVersion() {
        return versionRegistry.getVersionInfo(server.getCurrentVersionId());
    }



    public synchronized void rename(String newName) {
        server.setName(newName);
        server = repo.save(server);
    }

    public synchronized void delete() throws IllegalStateException, IOException {
        if (process != null && process.getStatus().isAlive()) {
            throw new IllegalStateException("Cannot delete a running server");
        }
        serverManager.drop(this);
        try {
            repo.deleteById(id);
        } catch (Exception e) {
            serverManager.revive(this);
            throw e;
        }
        FileUtils.deleteDirectory(getLocation().toFile());
    }


    
    public void start() {
        throw new NotImplementedException();
    }

    public void stop() {
        throw new NotImplementedException();
    }

    public void restart() {
        throw new NotImplementedException();
    }
}
