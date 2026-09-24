package de.maria_writes_code.mcsm.backend.features.runtimes;

import static de.maria_writes_code.mcsm.backend.App.LOGGER;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.codehaus.plexus.archiver.tar.TGZUnArchiver;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

public class AdoptiumRuntime implements JavaRuntime {
    private static final int CONN_TIMEOUT = 10_000, READ_TIMEOUT = 10_000;

    private static final UriBuilderFactory URI_FACTORY = new DefaultUriBuilderFactory();
    //https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jre/hotspot/normal/eclipse?project=jdk
    private static final UriBuilder RELEASE_URI_BUILDER = URI_FACTORY.builder()
        .scheme("https")
        .host("api.adoptium.net")
        .pathSegment("v3", "binary", "latest", "{javaVersion}", "ga", "linux", "x64", "jre", "hotspot", "normal", "eclipse")
        .queryParam("project", "jdk")
        ;
    
    private static URL urlForRelease(int javaVersion) {
        try {
            return RELEASE_URI_BUILDER.build(javaVersion).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unexpectedly malformed URL", e);
        }
    }

    public AdoptiumRuntime(int javaVersion, Path runtimeBasePath, Consumer<String> updateReceiver) throws IOException {
        var tempFile = Files.createTempFile("java-runtime", Integer.toString(javaVersion)).toFile();
        // Copying to temporary file is faster than extracting directly from download
        updateReceiver.accept("Downloading runtime");
        FileUtils.copyURLToFile(urlForRelease(javaVersion), tempFile, CONN_TIMEOUT, READ_TIMEOUT);
        updateReceiver.accept("Runtime received. Extracting...");
        
        var path = runtimeBasePath.resolve(Integer.toString(javaVersion));
        var tempDir = Files.createTempDirectory("java-runtime-dir");
        var unarchiver = new TGZUnArchiver();
        unarchiver.setSourceFile(tempFile);
        unarchiver.setDestDirectory(tempDir.toFile());
        unarchiver.extract();
        updateReceiver.accept("Extracted. Moving to source");
        
        var subdirs = Files.list(tempDir).collect(Collectors.toList());
        if (subdirs.size() > 1) {
            LOGGER.warn("Runtime directory has multiple children before move, which should not be possible");
            throw new IOException("Runtime directory contains more than one child");
        } else if (subdirs.size() == 0) {
            throw new IOException("Runtime directory is empty");
        }
        FileUtils.moveDirectory(subdirs.get(0).toFile(), path.toFile());
        updateReceiver.accept("Move completed. Deleting temporary files");
        try {
            Files.delete(tempFile.toPath());
        } catch (IOException e) {
            LOGGER.warn("Failed to delete temporary archive for java runtime. Ignoring", e);
        }
        FileUtils.deleteDirectory(tempDir.toFile());
        updateReceiver.accept("Runtime complete");
        
        this(path);
    }

    private final Path executable;

    /**
     * 
     * @param path
     * @throws IllegalArgumentException
     */
    public AdoptiumRuntime(Path path) throws IllegalArgumentException {
        executable = path.resolve("bin", "java");
        if (!executable.toFile().isFile() || !executable.toFile().canExecute()) {
            throw new IllegalArgumentException("Runtime does not have an executable bin/java file");
        }
    }

    @Override
    public Path getExecutable() {
        return executable;
    }
}
