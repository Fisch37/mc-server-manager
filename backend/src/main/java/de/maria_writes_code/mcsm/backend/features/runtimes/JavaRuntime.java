package de.maria_writes_code.mcsm.backend.features.runtimes;

import java.nio.file.Path;
import java.util.List;

public interface JavaRuntime {
    /**
     * Get a path to the executable java.
     */
    Path getExecutable();

    default List<String> getArguments(Path serverExecutable) {
        return List.of(
            "-jar",
            serverExecutable.toString(),
            "nogui"
        );
    }
}
