package de.maria_writes_code.mcsm.backend.features.templates;

import de.maria_writes_code.mcsm.backend.Utils;
import de.maria_writes_code.mcsm.backend.features.server.Server;

public class ServerBuilder {
    private final Server server;
    private ServerTemplate template;

    public ServerBuilder() {
        server = new Server(null, null);
    }

    public ServerBuilder setName(String name) {
        server.setName(name);
        return this;
    }
    public ServerBuilder setVersion(String versionId) {
        if (!Utils.contains(template.getDefinition().versions(), v -> v.id(), versionId)) {
            throw new IllegalArgumentException(
                "Template %s does not support version %s".formatted(
                    template.getDefinition().name(),
                    versionId
                )
            );
        }
        server.setCurrentVersionId(versionId);
        return this;
    }

    public ServerBuilder setTemplate(ServerTemplate template) {
        this.template = template;
        return this;
    }

    public Server build() {
        if (server.getName() == null) {
            throw new IllegalStateException("Server does not have a name");
        }
        if (server.getCurrentVersionId() == null) {
            throw new IllegalStateException("Server does not have a version");
        }
        return server;
    }
}
