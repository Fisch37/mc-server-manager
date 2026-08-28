package de.maria_writes_code.mcsm.backend.features.templates;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.maria_writes_code.mcsm.backend.AppConfig;
import de.maria_writes_code.mcsm.backend.features.components.ComponentRegistry;
import de.maria_writes_code.mcsm.backend.features.components.VanillaVersionRegistry;
import de.maria_writes_code.mcsm.backend.features.components.VersionCombo;
import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.Server;
import de.maria_writes_code.mcsm.backend.features.server.ServerManager;
import de.maria_writes_code.mcsm.backend.utils.Utils;

@NullMarked
public class ServerBuilder {
    private final ServerBuilder.Context context;

    private final Server server;
    private ServerTemplate template;

    public ServerBuilder(ServerBuilder.Context context) {
        this.context = context;
        server = new Server(null, new LinkedHashMap<>());
    }

    public ServerBuilder setName(String name) {
        server.setName(name);
        return this;
    }
    public ServerBuilder setVersions(VersionCombo versionIds) throws IOException, IllegalArgumentException {
        for (var entry : versionIds.getVersions().entrySet()) {
            var versionProviderId = entry.getKey();
            var versionId = entry.getValue();
            var source = context.componentRegistry.getVersionProvider(versionProviderId);
            if (source == null) {
                throw new IllegalArgumentException(
                    "Version source %s does not exist".formatted(versionProviderId)
                );
            }
            var version = source.getVersionInfo(versionId);
            if (version == null) {
                throw new IllegalArgumentException(
                    "Version %s does not exist on server %s".formatted(
                        versionId, versionProviderId
                    )
                );
            }

            server.setCurrentVersionId(versionProviderId, versionId);
        }
        var properties = context.componentRegistry.getComponent(template.getDefinition().type())
            .fetchVersionPropertiesGeneric(versionIds);
        // TODO: This strongly depends on java server types. It should be replaced with a system that is independent of server type semantics.
        server.setJavaVersion(Integer.parseInt(properties.get("java-version")));
        
        return this;
    }

    public ServerBuilder setTemplate(ServerTemplate template) {
        this.template = template;
        server.setTemplateId(template.getDefinition().id());
        server.setType(template.getDefinition().type());
        return this;
    }

    public ActiveServer build() throws IOException {
        if (server.getName() == null) {
            throw new IllegalStateException("Server does not have a name");
        }
        if (
            !server.getCurrentVersionIds().keySet()
                .containsAll(
                    template.getDefinition().getHierarchy(context.templateProvider)
                        .map(template -> context.componentRegistry.getComponent(template.type()))
                        .flatMap(c -> c.getVersionProviders().stream())
                        .map(vp -> vp.getSourceIdentifier())
                        .collect(Collectors.toSet())
                )
        ) {
            throw new IllegalStateException("Server does not have a version");
        }
        if (template == null || !template.getDefinition().id().equals(server.getTemplateId())) {
            throw new IllegalStateException("Server does not have a template, or has a broken template id");
        }
        server.setLastExitCode(0);
        template.apply(
            context.config.getServerLocation().resolve(server.getId().toString()),
            new VersionCombo.Mapped(server.getCurrentVersionIds())
        );
        return new ActiveServer(context.activeServerContext, server);
    }

    @Component
    public static class Context implements InitializingBean {
        @Autowired
        private VanillaVersionRegistry versionRegistry;
        @Autowired
        private AppConfig config;
        @Autowired
        private ActiveServer.Context activeServerContext;
        @Autowired
        private ServerManager _serverManager;
        
        @Autowired
        private TemplateProvider templateProvider;
        @Autowired
        private ComponentRegistry componentRegistry;
        
        @Override
        public void afterPropertiesSet() throws Exception {
            Utils.requireNonNull(versionRegistry, config);
            activeServerContext.setServerManager(_serverManager);
        }
    }
}
