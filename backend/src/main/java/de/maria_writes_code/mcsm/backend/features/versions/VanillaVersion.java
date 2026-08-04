package de.maria_writes_code.mcsm.backend.features.versions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.NotImplementedException;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class VanillaVersion implements Version {
    static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    ;

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
        return fetchVersionDetailsConcrete();
    }

    public VanillaDetails fetchVersionDetailsConcrete() throws IOException {
        var tree = MAPPER.readTree(metadataUrl.openStream());
        return new VanillaDetails(tree);
    }

    public record VanillaDetails(int javaVersion, URL serverJar) implements Details {
        private VanillaDetails(JsonNode details) {
            var javaVersion = details.get("javaVersion");
            if (javaVersion == null) {
                throw new NotImplementedException("TODO: Throw a good exception here");
            }
            var majorVersion = javaVersion.get("majorVersion");
            if (majorVersion == null) {
                throw new NotImplementedException("TODO: Throw a good exception here");
            }

            var urlString = details.get("downloads")
                .get("server")
                .get("url")
                .asText();
            if (urlString.isEmpty()) {
                throw new NotImplementedException("TODO: Throw a good exception here");
            }
            URL url;
            try {
                url = new URI(urlString).toURL();
            } catch (URISyntaxException | MalformedURLException e) {
                throw new NotImplementedException("TODO: Throw a good exception here", e);
            }
            this(majorVersion.asInt(), url);
        }
    }
}
