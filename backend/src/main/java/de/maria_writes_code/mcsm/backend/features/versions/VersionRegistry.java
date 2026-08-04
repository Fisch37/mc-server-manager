package de.maria_writes_code.mcsm.backend.features.versions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.IOUtils;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
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
     * @return The download process
     */
    public void getExecutable(String versionId, OutputStream destination) throws IOException {
        var version = versions.get(versionId);
        if (version == null) {
            throw new IllegalArgumentException("Version does not exist");
        }
        var url = version.fetchVersionDetailsConcrete().serverJar();
        try(var data = url.openStream()) {
            IOUtils.copy(data, destination);
            // data.transferTo(destination);
        }
    }

    private record ManifestVersion(String id, URL url) {
        public VanillaVersion toVanilla() {
            return new VanillaVersion(id, url);
        }
    }
}
