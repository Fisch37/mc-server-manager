package de.maria_writes_code.mcsm.backend.features.versions;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;

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
public class VersionRegistry {
    private static final URI MANIFEST_URL;
    static {
        try {
            MANIFEST_URL = new URI("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    private ConcurrentMap<String, VanillaVersion> versions;

    public VersionRegistry() throws IOException {
        JsonNode versionsRaw = VanillaVersion.MAPPER.readTree(MANIFEST_URL.toURL().openStream())
            .get("versions");
        if (!versionsRaw.isArray()) {
            throw new IOException("Malformed manifest: Expected a versions array");
        }
        versions = new ConcurrentHashMap<>();
        for (var version : versionsRaw) {
            var manifest = VanillaVersion.MAPPER.treeToValue(version, ManifestVersion.class);
            versions.put(manifest.id(), manifest.toVanilla());
        }
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
    public void getExecutable(String versionId, OutputStream destination) {
        throw new NotImplementedException();
    }

    private record ManifestVersion(String id, URL url) {
        public VanillaVersion toVanilla() {
            return new VanillaVersion(id, url);
        }
    }
}
