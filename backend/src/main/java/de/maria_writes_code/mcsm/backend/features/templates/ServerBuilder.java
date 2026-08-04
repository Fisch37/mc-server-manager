package de.maria_writes_code.mcsm.backend.features.templates;

import java.io.IOException;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.maria_writes_code.mcsm.backend.CustomAppConfig;
import de.maria_writes_code.mcsm.backend.Utils;
import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.Server;
import de.maria_writes_code.mcsm.backend.features.server.ServerManager;
import de.maria_writes_code.mcsm.backend.features.versions.VersionRegistry;

@NullMarked
public class ServerBuilder {
    private final ServerBuilder.Context context;

    private final Server server;
    private ServerTemplate template;

    public ServerBuilder(ServerBuilder.Context context) {
        this.context = context;
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
        var version = context.versionRegistry.getVersionInfo(versionId);
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
        server.setTemplateId(template.getDefinition().id());
        return this;
    }

    public ActiveServer build() throws IOException {
        if (server.getName() == null) {
            throw new IllegalStateException("Server does not have a name");
        }
        if (server.getCurrentVersionId() == null) {
            throw new IllegalStateException("Server does not have a version");
        }
        if (template == null || !template.getDefinition().id().equals(server.getTemplateId())) {
            throw new IllegalStateException("Server does not have a template, or has a broken template id");
        }
        server.setLastExitCode(0);
        template.apply(
            context.config.getServerLocation().resolve(server.getId().toString()),
            server.getCurrentVersionId()
        );
        return new ActiveServer(context.activeServerContext, server);
    }

    @Component
    public static class Context implements InitializingBean {
        @Autowired
        private VersionRegistry versionRegistry;
        @Autowired
        private CustomAppConfig config;
        @Autowired
        private ActiveServer.Context activeServerContext;
        @Autowired
        private ServerManager _serverManager;
        
        @Override
        public void afterPropertiesSet() throws Exception {
            Utils.requireNonNull(versionRegistry, config);
            activeServerContext.setServerManager(_serverManager);
        }
    }
}
