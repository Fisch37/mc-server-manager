package de.maria_writes_code.mcsm.backend.features.versions;

import java.io.IOException;

public interface Version {
    String id();
    Version.Details fetchVersionDetails() throws IOException;

    public interface Details {
        int javaVersion();
    }
}
