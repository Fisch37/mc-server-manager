package de.maria_writes_code.mcsm.backend.features.server;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.maria_writes_code.mcsm.backend.CustomAppConfig;
import de.maria_writes_code.mcsm.backend.Utils;
import de.maria_writes_code.mcsm.backend.features.runtimes.RuntimeProvider;
import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplate;
import de.maria_writes_code.mcsm.backend.features.templates.TemplateProvider;
import de.maria_writes_code.mcsm.backend.features.versions.Version;
import de.maria_writes_code.mcsm.backend.features.versions.VersionRegistry;

@NullMarked
public class ActiveServer {
    private final Context context;

    private final UUID id;

    // TODO: Long lived entities are not supported in JPA
    //  repo.save will overwrite changes made by other transactions
    private Server server;
    @Nullable
    private ServerProcess process;
    private ServerStatus status;
    
    public ActiveServer(Context context, Server server) {
        this.context = context;
        this.server = new Server(server);
        this.id = this.server.getId();
        this.status = this.server.hasCrashed() ? ServerStatus.Crashed : ServerStatus.Stopped;
    }

    public UUID getId() {
        return id;
    }

    public Server getServer() {
        return server;
    }

    public Path getLocation() {
        return context.appConfig.getServerLocation().resolve(id.toString());
    }

    public @Nullable Version getVersion() {
        return context.versionRegistry.getVersionInfo(server.getCurrentVersionId());
    }

    public ServerStatus getStatus() {
        return status;
    }

    private @Nullable ServerTemplate getTemplate() {
        return context.templateProvider.getTemplate(server.getTemplateId());
    }



    public synchronized void rename(String newName) {
        server.setName(newName);
        server = context.repo.save(server);
    }

    public synchronized void delete() throws IllegalStateException, IOException {
        if (process != null && status.isAlive()) {
            throw new IllegalStateException("Cannot delete a running server");
        }
        context.serverManager.drop(this);
        try {
            context.repo.deleteById(id);
        } catch (Exception e) {
            context.serverManager.revive(this);
            throw e;
        }
        FileUtils.deleteDirectory(getLocation().toFile());
    }


    
    public void start() throws IOException {
        var executable = getTemplate().getDefinition().executable();
        var runtime = context.runtimeProvider.getRuntime(server.getJavaVersion());
        if (runtime == null) {
            throw new IllegalStateException("Runtime for server does not exist");
        }
        var args = new ArrayList<String>();
        args.add(runtime.getExecutable().toString());
        args.addAll(executable.arguments());
        args.addAll(runtime.getArguments(getLocation().resolve(executable.file())));
        var process = new ProcessBuilder(args)
            .directory(getLocation().toFile())
            .start();
        this.process = new ServerProcess(process, Terminator.create(executable.terminator()));
        status = ServerStatus.Starting;
        
        // TODO: Wait for starting to finish
    }

    /**
     * Stop the server, if it is running.
     * @throws IllegalStateException The server is not currently running
     * @throws IOException Some I/O error occured while trying to stop the server
     */
    public void stop() throws IllegalStateException, IOException {
        if (process != null) {
            status = ServerStatus.Stopping;
            process.stop();
            status = ServerStatus.Stopped;
            synchronized (this) {
                server.setLastExitCode(process.getExitValue());
                server = context.repo.save(server);
            }
        } else {
            throw new IllegalStateException("Tried to stop the server, but it isn't running");
        }
    }

    /**
     * Restart the server, if it is running
     * @throws IOException Some I/O error occured while trying to restart the server
     */
    public void restart() throws IOException {
        stop();
        start();
    }

    public void sendCommand(String line) throws IOException {
        process.sendCommand(line);
    }

    @Component
    public static class Context implements InitializingBean {
        @Autowired
        private CustomAppConfig appConfig;
        @Autowired
        private ServerRepository repo;
        // @Autowired
        private ServerManager serverManager;
        @Autowired
        private VersionRegistry versionRegistry;
        @Autowired
        private TemplateProvider templateProvider;
        @Autowired
        private RuntimeProvider runtimeProvider;
        @Override
        public void afterPropertiesSet() throws Exception {
            Utils.requireNonNull(appConfig, repo, versionRegistry, templateProvider, runtimeProvider);
        }

        public void setServerManager(ServerManager serverManager) {
            this.serverManager = serverManager;
        }
    }
}
