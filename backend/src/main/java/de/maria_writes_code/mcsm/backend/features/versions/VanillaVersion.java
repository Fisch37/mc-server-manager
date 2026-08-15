package de.maria_writes_code.mcsm.backend.features.versions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.maria_writes_code.mcsm.backend.utils.Mutex;

public class VanillaVersion implements Version {
    static final ObjectMapper MAPPER = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    ;

    private String id;
    private String channel;
    private URL metadataUrl;

    private final Mutex<@Nullable VanillaDetails> details = new Mutex<>(null);

    public VanillaVersion(String id, URL metadataUrl) {
        this.id = id;
        this.metadataUrl = metadataUrl;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public String channel() {
        return channel;
    }

    @Override
    public Details fetchVersionDetails() throws IOException {
        return fetchVersionDetailsConcrete();
    }

    public VanillaDetails fetchVersionDetailsConcrete() throws IOException {
        synchronized (this.details) {
            if (details.get() != null)
                return details.get();
            var tree = MAPPER.readTree(metadataUrl.openStream());
            details.set(new VanillaDetails(id, tree));
            return details.get();
        }
    }

    public record VanillaDetails(String versionId, int javaVersion, URL serverJar, String serverSha1) implements Details {
        private VanillaDetails(String versionId, JsonNode details) {
            var javaVersion = details.get("javaVersion");
            if (javaVersion == null) {
                throw new NotImplementedException("TODO: Throw a good exception here");
            }
            var majorVersion = javaVersion.get("majorVersion");
            if (majorVersion == null) {
                throw new NotImplementedException("TODO: Throw a good exception here");
            }

            var download = details.get("downloads")
                .get("server");
            if (download == null) {
                // version does not support servers
                throw new NotImplementedException("TODO: Throw a good exception here");
            }
            var urlString = download.get("url").asText();
            if (urlString.isEmpty()) {
                throw new NotImplementedException("TODO: Throw a good exception here");
            }
            URL url;
            try {
                url = new URI(urlString).toURL();
            } catch (URISyntaxException | MalformedURLException e) {
                throw new NotImplementedException("TODO: Throw a good exception here", e);
            }
            
            var sha1 = download.get("sha1").asText();
            if (sha1.isEmpty()) {
                throw new NotImplementedException("TODO: Throw a good exception here");
            }

            this(versionId, majorVersion.asInt(), url, sha1);
        }
    }
}
