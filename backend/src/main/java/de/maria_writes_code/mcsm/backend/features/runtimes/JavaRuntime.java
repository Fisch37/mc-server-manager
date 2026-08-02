package de.maria_writes_code.mcsm.backend.features.runtimes;

import java.nio.file.Path;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import de.maria_writes_code.mcsm.backend.CustomAppConfig;

public class JavaRuntime {
    @Autowired
    private CustomAppConfig config;

    private int javaVersion;

    public JavaRuntime(int javaVersion) {
        this.javaVersion = javaVersion;
    }

    public Path getExecutable() {
        return config.getRuntimeLocation().resolve(Integer.toString(javaVersion));
    }

    public List<String> getArguments(Path serverExecutable) {
        return List.of(
            "-jar",
            serverExecutable.toString(),
            "nogui"
        );
    }
}
