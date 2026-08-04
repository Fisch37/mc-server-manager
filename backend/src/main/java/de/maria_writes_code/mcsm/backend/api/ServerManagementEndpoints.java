package de.maria_writes_code.mcsm.backend.api;

import static de.maria_writes_code.mcsm.backend.api.EndpointConsts.NO_SERVER_EXISTS;

import java.io.IOException;
import java.util.UUID;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import de.maria_writes_code.mcsm.backend.features.server.ActiveServer;
import de.maria_writes_code.mcsm.backend.features.server.Server;
import de.maria_writes_code.mcsm.backend.features.server.ServerManager;
import de.maria_writes_code.mcsm.backend.features.templates.ServerBuilder;
import de.maria_writes_code.mcsm.backend.features.templates.TemplateProvider;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("server")
public class ServerManagementEndpoints {
    @Autowired
    private ServerManager manager;
    @Autowired
    private TemplateProvider templateProvider;
    @Autowired
    private ServerBuilder.Context builderServices;

    @PostMapping("new")
    public ServerObject createServer(@RequestBody ServerBuilderObject builderParams) {
        var builder = new ServerBuilder(builderServices);
        try {
            builderParams.apply(builder, templateProvider);
            var server = builder.build();
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

    @PutMapping("{id}/name")
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

    public record ServerObject(UUID id, String name) {
        public ServerObject(ActiveServer server) {
            this(server.getServer());
        }

        public ServerObject(Server server) {
            this(server.getId(), server.getName());
        }
    }
    public record ServerBuilderObject(String name, String template, String version) {
        public void apply(ServerBuilder builder, TemplateProvider templates) throws IOException {
            builder.setName(name)
                .setTemplate(templates.getTemplate(template))
                .setVersion(version);
        }
    }
}
