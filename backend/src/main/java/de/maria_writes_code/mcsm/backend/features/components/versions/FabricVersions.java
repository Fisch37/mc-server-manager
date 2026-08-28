package de.maria_writes_code.mcsm.backend.features.components.versions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.function.Supplier;

import org.jspecify.annotations.NullMarked;
import org.springframework.web.util.UriTemplate;

import com.fasterxml.jackson.databind.JsonNode;

import de.maria_writes_code.mcsm.backend.features.components.FabricComponent;
import de.maria_writes_code.mcsm.backend.features.components.VanillaVersionRegistry;
import de.maria_writes_code.mcsm.backend.features.components.VersionCombo;
import de.maria_writes_code.mcsm.backend.utils.Utils;

@NullMarked
public record FabricVersions(String loader, String installer, String minecraft) implements VersionCombo {
    private static final UriTemplate EXECUTABLE_URI_TEMPLATE = new UriTemplate("https://meta.fabricmc.net/v2/versions/loader/{minecraft}/{loader}/{installer}/server/jar");

    public FabricVersions(VersionCombo maybeCompatible) throws IllegalArgumentException {
        Supplier<IllegalArgumentException> error = () -> new IllegalArgumentException(
            "provided version combo cannot be converted into a FabricVersion. Missing some keys. Provided: %s, required: {%s, %s, %s}"
                .formatted(
                    maybeCompatible.getVersions().keySet(),
                    FabricComponent.LOADER_ID, FabricComponent.INSTALLER_ID, VanillaVersionRegistry.VERSION_ID
                )
        );
        this(
            maybeCompatible.getVersion(FabricComponent.LOADER_ID)
                .orElseThrow(error),
            maybeCompatible.getVersion(FabricComponent.INSTALLER_ID)
                .orElseThrow(error),
            maybeCompatible.getVersion(VanillaVersionRegistry.VERSION_ID)
                .orElseThrow(error)
        );
    }

    @Override
    public Map<String, String> getVersions() {
        return Map.of(
            FabricComponent.LOADER_ID, loader,
            FabricComponent.INSTALLER_ID, installer,
            VanillaVersionRegistry.VERSION_ID, minecraft
        );
    }

    public URL getJarURL() {
        try {
            return EXECUTABLE_URI_TEMPLATE.expand(minecraft, loader, installer).toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException("Template replacement did not result in correct URL. Should have been impossible", e);
        }
    }

    
    public record LoaderVersion(String id, boolean isStable) implements Version {
        public LoaderVersion(JsonNode json) throws IllegalArgumentException {
            var stable = json.get("stable");
            if (!stable.isBoolean()) {
                throw new IllegalArgumentException("Fabric manifest does not contain boolean field \"stable\"");
            }
            this(
                Utils.throwIfNull(
                    json.get("version").asText(),
                    () -> new IllegalArgumentException("Json node does not contain required field \"version\"")
                ),
                stable.asBoolean()
            );
        }

        @Override
        public String channel() {
            return isStable ? "stable" : "unstable";
        }
    }

    public record InstallerVersion(String id, boolean isStable) implements Version {
        public InstallerVersion(JsonNode json) throws IllegalArgumentException {
            var stable = json.get("stable");
            if (!stable.isBoolean()) {
                throw new IllegalArgumentException("Fabric manifest does not contain boolean field \"stable\"");
            }
            this(
                Utils.throwIfNull(
                    json.get("version").asText(),
                    () -> new IllegalArgumentException("Json node does not contain required field \"version\"")
                ),
                stable.asBoolean()
            );
        }

        @Override
        public String channel() {
            return isStable ? "stable" : "unstable";
        }
    }
}
