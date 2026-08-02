package de.maria_writes_code.mcsm.backend.features.versions;

import java.io.OutputStream;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

/**
 * Provides metadata about available versions.
 * 
 * @apiNote
 *  Note that this registry does not guarantee permanence of its versions.
 *  Particularly, you should not rely on the existence of a version across long stretches of time.
 *  You should also not rely on a version not being present.
 *  No guarantees are made about the format of valid version ids.
 */
@Service
public class VersionRegistry {
    /**
     * Get metadata about a version
     * @param versionId Should be a version id, though if the string is invalid, {@code null} is returned.
     * @return A valid version or null if the version could not be found in the registry.
     * @see VersionRegistry
     */
    public @Nullable Version getVersionInfo(String versionId) {
        throw new NotImplementedException();
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
}
