package de.maria_writes_code.mcsm.backend.features.components.versions;

import java.util.Collection;

import org.springframework.stereotype.Component;

@Component
public interface VersionProvider {
    String getSourceIdentifier();
    String getFriendlyName();
    Version getVersionInfo(String versionId);
    /**
     * 
     * @param versionId
     * @return -1 if no version of the given id was found, else some index of the version.
     */
    int indexOf(String versionId);
    Collection<? extends Version> getVersions();
}
