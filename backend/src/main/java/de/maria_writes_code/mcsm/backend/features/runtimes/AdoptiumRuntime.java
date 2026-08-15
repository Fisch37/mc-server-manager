package de.maria_writes_code.mcsm.backend.features.runtimes;

import static de.maria_writes_code.mcsm.backend.App.LOGGER;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

public class AdoptiumRuntime implements JavaRuntime {
    private static final int CONN_TIMEOUT = 10_000, READ_TIMEOUT = 10_000;

    // 16MiBs seems reasonable for a large archive file
    private static final CompressorStreamFactory COMPRESSOR_FACTORY = new CompressorStreamFactory(false, 16*1024);
    private static final ArchiveStreamFactory ARCHIVE_FACTORY = new ArchiveStreamFactory();
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

    public AdoptiumRuntime(int javaVersion, Path runtimeBasePath) throws IOException {
        var tempFile = Files.createTempFile("java-runtime", Integer.toString(javaVersion)).toFile();
        // Copying to temporary file is faster than extracting directly from download
        FileUtils.copyURLToFile(urlForRelease(javaVersion), tempFile, CONN_TIMEOUT, READ_TIMEOUT);
        
        var path = runtimeBasePath.resolve(Integer.toString(javaVersion));
        var tempDir = Files.createTempDirectory("java-runtime-dir");
        try (
            var decompStream = COMPRESSOR_FACTORY.createCompressorInputStream(
                new BufferedInputStream(new FileInputStream(tempFile))
            );
            var archiveStream = ARCHIVE_FACTORY.createArchiveInputStream(
                new BufferedInputStream(decompStream)
            )
        ) {
            archiveStream.forEach(entry -> {
                Path localPath = entry.resolveIn(tempDir);
                if (entry.isDirectory()) {
                    try {
                        Files.createDirectories(localPath);
                    } catch (FileAlreadyExistsException e) {
                        LOGGER.warn("Runtime download tried to create an existing directory " + localPath, e);
                    }
                } else {
                    try (var fileOutput = new FileOutputStream(localPath.toFile())) {
                        IOUtils.copy(archiveStream, fileOutput);
                    }
                }
            });
        }
        
        var subdirs = Files.list(tempDir).collect(Collectors.toList());
        if (subdirs.size() > 1) {
            LOGGER.warn("Runtime directory has multiple children before move, which should not be possible");
        } else if (subdirs.size() == 0) {
            throw new IOException("Runtime directory is empty");
        }
        FileUtils.moveDirectory(subdirs.get(0).toFile(), path.toFile());
        try {
            Files.delete(tempFile.toPath());
        } catch (IOException e) {
            LOGGER.warn("Failed to delete temporary archive for java runtime. Ignoring", e);
        }
        FileUtils.deleteDirectory(tempDir.toFile());
        
        this(path);
        throw new NotImplementedException("Runtimes produced by this constructor do not have any executable bits set and are thus unusable");
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
