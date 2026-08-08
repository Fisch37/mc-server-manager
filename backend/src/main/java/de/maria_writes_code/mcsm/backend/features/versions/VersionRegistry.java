package de.maria_writes_code.mcsm.backend.features.versions;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

import de.maria_writes_code.mcsm.backend.AppConfig;

import static de.maria_writes_code.mcsm.backend.App.LOGGER;

/**
 * Provides metadata about available versions.
 * 
 * @apiNote
 *  Note that this registry does not guarantee permanence of its versions.
 *  Particularly, you should not rely on the existence of a version across long stretches of time.
 *  You should also not rely on a version not being present.
 *  No guarantees are made about the format of valid version ids.
 */
@Service @Scope("singleton")
public class VersionRegistry implements InitializingBean {
    private static final URL MANIFEST_URL;
    static {
        try {
            MANIFEST_URL = new URI("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json").toURL();
        } catch (URISyntaxException|MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }

    private ConcurrentMap<String, VanillaVersion> versions;
    @Autowired
    private AppConfig config;
    @Autowired
    private ServerJarRepository.Vanilla vanillaJars;

    public VersionRegistry() { }

    @Override
    public void afterPropertiesSet() throws IOException {
        var request = MANIFEST_URL.openStream();
        JsonNode versionsRaw = VanillaVersion.MAPPER.readTree(request)
            .get("versions");
        if (!versionsRaw.isArray()) {
            throw new IOException("Malformed manifest: Expected a versions array");
        }
        versions = new ConcurrentHashMap<>();
        for (var version : versionsRaw) {
            var manifest = VanillaVersion.MAPPER.treeToValue(version, ManifestVersion.class);
            versions.put(manifest.id(), manifest.toVanilla());
        }
        LOGGER.info("Fetched available versions!");
    }

    /**
     * Get metadata about a version
     * @param versionId Should be a version id, though if the string is invalid, {@code null} is returned.
     * @return A valid version or null if the version could not be found in the registry.
     * @see VersionRegistry
     */
    public @Nullable Version getVersionInfo(String versionId) {
        return versions.get(versionId);
    }

    /**
     * Download the executable for the specified version.
     * @param versionId The version to download for
     * @param destination The stream into which the executable is downloaded.
     */
    public void getExecutable(String versionId, OutputStream destination) throws IOException {
        var version = versions.get(versionId);
        if (version == null) {
            throw new IllegalArgumentException("Version does not exist");
        }
        var executablePath = ensureExecutable(version);
        Files.copy(executablePath, destination);
    }

    private Path ensureExecutable(VanillaVersion version) throws IOException {
        @Nullable Path path = vanillaJars.findByVersionId(version.id())
            .map(s -> executablePath(s.getSha1()))
            .orElse(null)
            ;
        if (path == null || !path.toFile().isFile()) {
            path = ensureExecutable(version.fetchVersionDetailsConcrete());
        }
        return path;
    }
    private Path ensureExecutable(VanillaVersion.VanillaDetails details) throws IOException {
        var path = executablePath(details);
        if (!path.toFile().isFile()) {
            fetchExecutable(details);
        }
        return path;
    }

    private Path fetchExecutable(VanillaVersion.VanillaDetails details) throws IOException {
        var destination = executablePath(details);
        var url = details.serverJar();
        try(
            var file = new BufferedOutputStream(new FileOutputStream(destination.toFile()));
            var data = url.openStream();
        ) {
            IOUtils.copy(data, file);
        }
        vanillaJars.save(new ServerJar.Vanilla(details.versionId(), details.serverSha1()));
        return destination;
    }

    private Path executablePath(VanillaVersion.VanillaDetails details) {
        return executablePath(details.serverSha1());
    }
    private Path executablePath(String sha1) {
        return config.getVanillaJarLocation().resolve(sha1 + ".jar");
    }

    private record ManifestVersion(String id, URL url) {
        public VanillaVersion toVanilla() {
            return new VanillaVersion(id, url);
        }
    }
}
