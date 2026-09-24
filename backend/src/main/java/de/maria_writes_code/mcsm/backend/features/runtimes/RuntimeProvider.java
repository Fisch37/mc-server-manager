package de.maria_writes_code.mcsm.backend.features.runtimes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import de.maria_writes_code.mcsm.backend.AppConfig;
import static de.maria_writes_code.mcsm.backend.App.LOGGER;

@Service @Scope("singleton")
@NullMarked
public class RuntimeProvider implements InitializingBean {
    private static final Map<Integer, Object> FETCH_LOCKS = new ConcurrentHashMap<>();

    @Autowired
    AppConfig config;

    private SortedMap<Integer, AdoptiumRuntime> runtimes;

    @Override
    public void afterPropertiesSet() throws Exception {
        runtimes = new TreeMap<>();
        for (
            var dir
            : (Iterable<Path>)(
                Files.list(config.getRuntimeLocation())
                    .filter(Files::isDirectory)
                    ::iterator
            )
        ) {
            // file name cannot be null, b/c dir must be a subdirectory of runtimeLocation
            int javaVersion;
            try {
                javaVersion = Integer.parseInt(dir.getFileName().toString());
            } catch (NumberFormatException e) {
                LOGGER.warn(
                    "Skipping runtime directory {} because its name is not a valid number",
                    dir
                );
                continue;
            }
            // no two runtimes with the same version can exist,
            // because that would require two folders of the same name
            runtimes.put(javaVersion, new AdoptiumRuntime(dir));
        }
    }

    public @Nullable JavaRuntime getRuntime(int javaVersion) {
        return runtimes.get(javaVersion);
    }

    /**
     * Gets some runtime with a version greater or equal to the supplied argument.
     * @param javaVersion The minimum version
     * @return Some java runtime with a version >= javaVersion, or null if no such runtime is available.
     */
    public @Nullable JavaRuntime getRuntimeSupporting(int javaVersion) {
        var entry = runtimes.tailMap(javaVersion)
            .firstEntry();
        return entry == null ? null : entry.getValue();
    }

    public JavaRuntime ensureRuntimeSupporting(int javaVersion, Consumer<String> updateReceiver) throws IOException {
        return ensureRuntime(javaVersion, v -> this.getRuntimeSupporting(v), updateReceiver);
    }

    public JavaRuntime ensureRuntime(int javaVersion, Consumer<String> updateReceiver) throws IOException {
        return ensureRuntime(javaVersion, v -> this.getRuntime(v), updateReceiver);
    }

    private JavaRuntime ensureRuntime(
        int javaVersion,
        Function<Integer, JavaRuntime> getter,
        Consumer<String> updateReceiver
    ) throws IOException {
        // TODO: This may still be vulnerable to race conditions
        synchronized (runtimes) {
            var res = getter.apply(javaVersion);
            if (res != null)
                return res;
        }
        Object lock;
        synchronized (FETCH_LOCKS) {
            lock = new Object();
            var existingLock = FETCH_LOCKS.putIfAbsent(javaVersion, lock);
            if (existingLock != null)
                lock = existingLock;
        }
        AdoptiumRuntime res;
        synchronized (lock) {
            res = runtimes.get(javaVersion);
            if (res != null)
                return res;
            res = new AdoptiumRuntime(javaVersion, config.getRuntimeLocation(), updateReceiver);
            runtimes.put(javaVersion, res);
        }
        return res;
    }
}
