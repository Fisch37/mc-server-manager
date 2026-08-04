package de.maria_writes_code.mcsm.backend.features.runtimes;

import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilder;
import org.springframework.web.util.UriBuilderFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class AdoptiumRuntime implements JavaRuntime {
    private static final UriBuilderFactory URI_FACTORY = new DefaultUriBuilderFactory();
    //https://api.adoptium.net/v3/binary/latest/21/ga/linux/x64/jre/hotspot/normal/eclipse?project=jdk
    private static final UriBuilder RELEASE_URI_BUILDER = URI_FACTORY.builder()
        .scheme("https")
        .host("adpi.adoptium.net")
        .pathSegment("v3", "binary", "latest", "{javaVersion}", "ga", "linux", "x64", "jre", "hotspot", "normal", "eclipse")
        .queryParam("project", "jdk")
        ;
    private static final ObjectMapper MAPPER = new ObjectMapper();
    
    private static URL urlForRelease(int javaVersion) {
        try {
            return RELEASE_URI_BUILDER.build(javaVersion).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Unexpectedly malformed URL", e);
        }
    }

    public AdoptiumRuntime(int javaVersion) throws IOException {
        var connection = (HttpURLConnection)urlForRelease(javaVersion).openConnection();
        var responseCode = connection.getResponseCode();
        if (responseCode >= 300 || responseCode < 200) {
            throw new NotImplementedException("TODO: Throw a good error here");
        }
        var destFile = Files.createTempFile("java-runtime", "");
        try (var destination = new FileOutputStream(destFile.toFile())) {
            IOUtils.copy(connection.getInputStream(), destination);
        }
        // TODO: Extract and copy to destination
    }

    private Path executable;

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
