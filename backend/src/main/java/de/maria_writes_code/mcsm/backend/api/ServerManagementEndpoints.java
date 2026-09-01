package de.maria_writes_code.mcsm.backend.api;

import static de.maria_writes_code.mcsm.backend.api.EndpointUtils.NO_SERVER_EXISTS;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.maria_writes_code.mcsm.backend.api.websockets.ListenerSocket;
import de.maria_writes_code.mcsm.backend.features.components.ComponentRegistry;
import de.maria_writes_code.mcsm.backend.features.components.VersionCombo;
import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.ServerManager;
import de.maria_writes_code.mcsm.backend.features.server.ServerStatus;
import de.maria_writes_code.mcsm.backend.features.templates.ServerBuilder;
import de.maria_writes_code.mcsm.backend.features.templates.TemplateProvider;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("server")
public class ServerManagementEndpoints {
    @Autowired
    private WebsocketGateway gateway;
    @Autowired
    private ServerManager manager;
    @Autowired
    private TemplateProvider templateProvider;
    @Autowired
    private ComponentRegistry componentRegistry;

    @Autowired
    private ServerBuilder.Context builderServices;

    @PostMapping("new")
    public ServerObject createServer(@RequestBody ServerBuilderObject builderParams) {
        return createServer(builderParams, l -> { });
    }
    @PostMapping(value = "new/follow", produces = "text/plain") 
    public String createServerFollow(@RequestBody ServerBuilderObject builderParams) {
        var ws = new ListenerSocket();
        new Thread(() -> {
            try {
                createServer(builderParams, ws);
            } catch (Exception e) {
                try (var stringWriter = new StringWriter(); var printWriter = new PrintWriter(stringWriter)) {
                    e.printStackTrace(printWriter);
                    ws.accept(stringWriter.toString());
                } catch (IOException ioe) {
                    ws.accept("I/O error occurred trying to print stack trace of another error. Error is " + e.getMessage());
                    ioe.printStackTrace();
                }
            } finally {
                // close socket
                ws.accept(null);
            }
        }).start();
        return gateway.register(ws);
    }
    private ServerObject createServer(
        ServerBuilderObject builderParams,
        Consumer<String> updateReceiver
    ) {
        var builder = new ServerBuilder(builderServices);
        try {
            validateProperties(builderParams.template, builderParams.properties);
            builderParams.apply(builder, templateProvider);
            var server = builder.build(updateReceiver);
            manager.add(server);
            return new ServerObject(server);
        } catch (IOException e) {
            throw new ResponseStatusException(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "I/O error while creating the server",
                e
            );
        }
    }

    @GetMapping("list")
    public Stream<ServerObject> listServers() {
        return manager.stream().map(a -> new ServerObject(a));
    }

    @GetMapping("{id}")
    public ServerObject getServer(@PathVariable UUID id) {
        return manager.get(id)
            .map(ServerObject::new)
            .orElseThrow(NO_SERVER_EXISTS)
        ;
    }

    @DeleteMapping("{id}")
    @ResponseStatus(code = HttpStatus.NO_CONTENT)
    public void deleteServer(@PathVariable UUID id) {
        var server = manager.get(id).orElse(null);
        if (server == null) {
            throw NO_SERVER_EXISTS.get();
        } else {
            try {
                server.delete();
            } catch (IllegalStateException e) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Server is not stopped",
                    e
                );
            } catch (IOException e) {
                throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "I/O during server deletion",
                    e
                );
            }
        }
    }

    @PutMapping(value = "{id}/name", consumes = { MediaType.TEXT_PLAIN_VALUE })
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void renameServer(
        @PathVariable UUID id,
        @RequestBody String newName
    ) {
        var server = manager.get(id).orElse(null);
        if (server == null) {
            throw NO_SERVER_EXISTS.get();
        } else {
            server.rename(newName);
        }
    }

    private void validateProperties(String templateId, Map<String, String> properties) throws ResponseStatusException {
        var template = templateProvider.getTemplate(templateId);
        if (template == null)
            return; // Nothing to do. Later checks will detect this and throw the correct error
        var component = componentRegistry.getComponent(template.getDefinition().type());
        var allowedProperties = component.getAvailableProperties()
            .stream()
            .collect(Collectors.toMap(conf -> conf.getId(), Function.identity()))
            ;
        for (var entry : properties.entrySet()) {
            var propertyDescriptor = allowedProperties.get(entry.getKey());
            if (propertyDescriptor == null)
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Property \"%s\" was passed, but is not a configurable property"
                        .formatted(entry.getKey())
                );
            try {
                propertyDescriptor.validate(entry.getValue());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Property value for \"%s\" (template \"%s\") cannot have value \"%s\""
                        .formatted(entry.getKey(), templateId, entry.getValue()),
                    e
                );
            }
        }
    }

    public record ServerObject(UUID id, String name, ServerStatus status) {
        public ServerObject(ActiveServer server) {
            this(server.getId(), server.getServer().getName(), server.getStatus());
        }
    }
    public record ServerBuilderObject(
        String name,
        String template,
        Map<String, String> versions,
        Map<String, String> properties
    ) {
        public void apply(
            ServerBuilder builder,
            TemplateProvider templates
        ) throws IOException {
            var template = templates.getTemplate(this.template);
            builder.setName(name)
                .setTemplate(template)
                .setVersions(new VersionCombo.Mapped(versions))
                .setProperties(properties);
        }
    }
}
