package de.maria_writes_code.mcsm.backend.features.templates;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;

import de.maria_writes_code.mcsm.backend.CustomAppConfig;
import de.maria_writes_code.mcsm.backend.Utils;
import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.Server;
import de.maria_writes_code.mcsm.backend.features.server.ServerRepository;
import de.maria_writes_code.mcsm.backend.features.versions.VersionRegistry;

public class ServerBuilder {
    @Autowired
    private VersionRegistry versionRegistry;
    @Autowired
    private CustomAppConfig config;
    @Autowired
    private ServerRepository repo;

    private final Server server;
    private ServerTemplate template;

    public ServerBuilder() {
        server = new Server(null, null);
    }

    public ServerBuilder setName(String name) {
        server.setName(name);
        return this;
    }
    public ServerBuilder setVersion(String versionId) throws IOException {
        if (!Utils.contains(template.getDefinition().versions(), v -> v.id(), versionId)) {
            throw new IllegalArgumentException(
                "Template %s does not support version %s".formatted(
                    template.getDefinition().name(),
                    versionId
                )
            );
        }
        var version = versionRegistry.getVersionInfo(versionId);
        if (version == null) {
            throw new IllegalArgumentException(
                "Version %s does not exist on the remote server".formatted(
                    versionId
                )
            );
        }
        server.setCurrentVersionId(versionId);
        server.setJavaVersion(version.fetchVersionDetails().javaVersion());
        
        return this;
    }

    public ServerBuilder setTemplate(ServerTemplate template) {
        this.template = template;
        return this;
    }

    public ActiveServer build() throws IOException {
        if (server.getName() == null) {
            throw new IllegalStateException("Server does not have a name");
        }
        if (server.getCurrentVersionId() == null) {
            throw new IllegalStateException("Server does not have a version");
        }
        server.setLastExitCode(0);
        template.apply(config.getServerLocation().resolve(server.getId().toString()), server.getCurrentVersionId());
        return new ActiveServer(repo.save(server));
    }
}
