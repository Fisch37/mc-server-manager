package de.maria_writes_code.mcsm.backend.features.versions;

import java.io.IOException;
import java.net.URL;

import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VanillaVersion implements Version {
    static final ObjectMapper MAPPER = new ObjectMapper();

    private String id;
    private URL metadataUrl;

    public VanillaVersion(String id, URL metadataUrl) {
        this.id = id;
        this.metadataUrl = metadataUrl;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public Details fetchVersionDetails() throws IOException {
        var tree = MAPPER.readTree(metadataUrl.openStream());
        return new VanillaDetails(tree);
    }

    public record VanillaDetails(int javaVersion) implements Details {
        private VanillaDetails(JsonNode details) {
            var javaVersion = details.get("javaVersion");
            if (javaVersion == null) {
                throw new NotImplementedException("TODO: Throw a good exception here");
            }
            var majorVersion = javaVersion.get("majorVersion");
            if (majorVersion == null) {
                throw new NotImplementedException("TODO: Throw a good exception here");
            }
            this(majorVersion.asInt());
        }
    }
}
