package de.maria_writes_code.mcsm.backend.features.server;

import static de.maria_writes_code.mcsm.backend.App.LOGGER;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import de.maria_writes_code.mcsm.backend.AppConfig;
import de.maria_writes_code.mcsm.backend.features.runtimes.RuntimeProvider;
import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplate;
import de.maria_writes_code.mcsm.backend.features.templates.TemplateProvider;
import de.maria_writes_code.mcsm.backend.features.versions.Version;
import de.maria_writes_code.mcsm.backend.features.versions.VersionRegistry;
import de.maria_writes_code.mcsm.backend.utils.Observable;
import de.maria_writes_code.mcsm.backend.utils.ReadOnlyObserver;
import de.maria_writes_code.mcsm.backend.utils.Utils;

@NullMarked
public class ActiveServer {
    private static final List<String> POTENTIAL_LOG_LOCATIONS = List.of(
        "logs",
        "crash-reports"
    );

    private final Context context;

    private final UUID id;

    // TODO: Long lived entities are not supported in JPA
    //  repo.save will overwrite changes made by other transactions
    private Server server;
    @Nullable
    private ServerProcess process;
    private Observable<ServerStatus> status;
    
    public ActiveServer(Context context, Server server) {
        this.context = context;
        this.server = new Server(server);
        this.id = this.server.getId();
        this.status = new Observable<>(
            this.server.hasCrashed() ? ServerStatus.Crashed : ServerStatus.Stopped
        );
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
        return status.get();
    }
    public ReadOnlyObserver<ServerStatus> getStatusObserver() {
        return status;
    }

    public @Nullable ServerProcess getProcess() {
        return process;
    }

    private @Nullable ServerTemplate getTemplate() {
        return context.templateProvider.getTemplate(server.getTemplateId());
    }



    public synchronized void rename(String newName) {
        server.setName(newName);
        server = context.repo.save(server);
    }

    public synchronized void delete() throws IllegalStateException, IOException {
        if (process != null && getStatus().isAlive()) {
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
            .redirectErrorStream(true)
            .start();
        this.process = new ServerProcess(
            process,
            Terminator.create(executable.terminator()),
            exitValue -> status.set(Utils.isExitCodeOk(exitValue) ? ServerStatus.Stopped : ServerStatus.Crashed)
        );
        status.set(ServerStatus.Starting);
        
        // TODO: Wait for starting to finish
        status.set(ServerStatus.Started);
    }

    /**
     * Stop the server, if it is running.
     * @throws IllegalStateException The server is not currently running
     * @throws IOException Some I/O error occured while trying to stop the server
     */
    public void stop() throws IllegalStateException, IOException {
        if (process != null) {
            status.set(ServerStatus.Stopping);
            process.stop();
            status.set(Utils.isExitCodeOk(process.getExitValue()) ? ServerStatus.Stopped : ServerStatus.Crashed);
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

    /**
     * Get all log files in descending order by their date of modification.
     * @return A list of paths which are (at time of check) paths to existing log files
     * @throws IOException if an I/O error occurs
     */
    public List<LogFile> getLogFiles() throws IOException {
        List<LogFile> outputList = new ArrayList<>();
        for (var logLocation : POTENTIAL_LOG_LOCATIONS) {
            var logDir = getLocation().resolve(logLocation);
            try {
                Files.list(logDir)
                    .filter(p -> Files.isRegularFile(p, LinkOption.NOFOLLOW_LINKS))
                    .map(p -> LogFile.fromFileName(
                        p,
                        getLocation().relativize(p).toString())
                    )
                    .forEach(outputList::add);
            } catch (NoSuchFileException e) {
                // skip this location
            }
        }
        // Sorting after the fact because it's easier and only needs one allocation instead of n.
        // Note however that this is slower by n*log(k) where k is the amount of log sources.
        //  O(n*log(n)) if sort once vs O(k*(n/k)*log(n/k)) = O(n*log(n/k)) = O(n*(log(n)-log(k)))
        // (we would still need to sort after the fact, but this will be faster than O(nlogn)
        //  due to the way List.sort optimizes for partially sorted lists)
        outputList.sort(Comparator.<LogFile, FileTime>comparing(file -> {
            try {
                return Files.getLastModifiedTime(file.getLocation(), LinkOption.NOFOLLOW_LINKS);
            } catch (IOException e) {
                // FIXME: proper handling of I/O exceptions inside comparators.
                LOGGER.warn("I/O error while comparing file dates", e);
                // reasonable default
                return FileTime.from(Instant.EPOCH);
            }
        }).reversed());
        return outputList;
    }

    @Component
    public static class Context implements InitializingBean {
        @Autowired
        private AppConfig appConfig;
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
