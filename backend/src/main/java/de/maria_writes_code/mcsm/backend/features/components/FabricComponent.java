package de.maria_writes_code.mcsm.backend.features.components;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.hibernate.boot.registry.classloading.spi.ClassLoadingException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import de.maria_writes_code.mcsm.backend.features.components.configuration.ConfigurationDescriptor;
import de.maria_writes_code.mcsm.backend.features.components.configuration.MinecraftConfigurationHelper;
import de.maria_writes_code.mcsm.backend.features.components.configuration.ServerConfigurationHelper;
import de.maria_writes_code.mcsm.backend.features.components.execution_helpers.MinecraftExecutionHelper;
import de.maria_writes_code.mcsm.backend.features.components.execution_helpers.ServerExecutionHelper;
import de.maria_writes_code.mcsm.backend.features.components.versions.FabricVersions;
import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProvider;
import de.maria_writes_code.mcsm.backend.features.runtimes.RuntimeProvider;

@Service
public class FabricComponent implements ServerType<FabricVersions>, InitializingBean {
    public final static String LOADER_ID = "fabric-loader", INSTALLER_ID = "fabric-installer";

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final URL VERSIONS_ENDPOINT;
    static {
        try {
            VERSIONS_ENDPOINT = new URI("https://meta.fabricmc.net/v2/versions").toURL();
        } catch (MalformedURLException|URISyntaxException e) {
            throw new ClassLoadingException("Failed to load class "+FabricComponent.class.getName(), e);
        }
    }

    @Autowired
    private VanillaVersionRegistry vanillaRegistry;
    @Autowired
    private RuntimeProvider runtimeProvider;
    private InstallerProvider installer;
    private LoaderProvider loader;

    @Override
    public void afterPropertiesSet() throws Exception {
        Objects.requireNonNull(vanillaRegistry);
        JsonNode tree = MAPPER.readTree(new BufferedInputStream(VERSIONS_ENDPOINT.openStream()));
        loader = new LoaderProvider(Objects.requireNonNull(tree.get("loader")));
        installer = new InstallerProvider(Objects.requireNonNull(tree.get("installer")));
    }
    
    @Override
    public ComponentIdentifier getIdentifier() {
        return ComponentIdentifier.Fabric;
    }
    
    @Override
    public Collection<? extends VersionProvider> getVersionProviders() {
        return List.of(vanillaRegistry, installer, loader);
    }

    public InstallerProvider getInstallerProvider() {
        return installer;
    }
    public LoaderProvider getLoaderProvider() {
        return loader;
    }
    
    @Override
    public ServerExecutionHelper getExecutionHelper() {
        return new MinecraftExecutionHelper(runtimeProvider);
    }

    @Override
    public void fetchExecutableGeneric(VersionCombo versions, Path destination, Consumer<String> updateReceiver)
            throws IOException, IllegalArgumentException {
        fetchExecutable(new FabricVersions(versions), destination, updateReceiver);
    }

    @Override
    public void fetchExecutable(FabricVersions versions, Path destination, Consumer<String> updateReceiver) throws IOException {
        updateReceiver.accept("Downloading fabric jar");
        FileUtils.copyURLToFile(versions.getJarURL(), destination.toFile());
        updateReceiver.accept("Fabric download complete");
    }

    public static class LoaderProvider extends VersionProvider.LinkedMapProvider<FabricVersions.LoaderVersion> {
        public LoaderProvider(JsonNode tree) throws IllegalArgumentException {
            super(
                tree.valueStream()
                    .map(FabricVersions.LoaderVersion::new)
                    .collect(Collectors.toUnmodifiableList())
            );
        }

        @Override
        public String getSourceIdentifier() {
            return FabricComponent.LOADER_ID;
        }

        @Override
        public String getFriendlyName() {
            return "Loader Version";
        }

        @Override
        public Set<String> getDefaultChannels() {
            return Set.of(FabricVersions.LoaderVersion.STABLE_CHANNEL_ID);
        }
    }

    public static class InstallerProvider extends VersionProvider.LinkedMapProvider<FabricVersions.InstallerVersion> {
        public InstallerProvider(JsonNode tree) throws IllegalArgumentException {
            super(
                tree.valueStream()
                    .map(FabricVersions.InstallerVersion::new)
                    .collect(Collectors.toUnmodifiableList())
            );
        }

        @Override
        public String getSourceIdentifier() {
            return FabricComponent.INSTALLER_ID;
        }

        @Override
        public String getFriendlyName() {
            return "Fabric Installer Version";
        }

        @Override
        public Set<String> getDefaultChannels() {
            return Set.of(FabricVersions.InstallerVersion.STABLE_CHANNEL_ID);
        }
        
    }

    @Override
    public Map<String, String> fetchVersionProperties(FabricVersions versions) throws IOException {
        return fetchVersionPropertiesGeneric(versions);
    }

    @Override
    public Map<String, String> fetchVersionPropertiesGeneric(VersionCombo versions)
            throws IOException, IllegalArgumentException {
        return vanillaRegistry.fetchVersionPropertiesGeneric(versions);
    }

    @Override
    public List<ConfigurationDescriptor<?>> getAvailableProperties() {
        return vanillaRegistry.getAvailableProperties();
    }

    @Override
    public ServerConfigurationHelper getConfigurationHelper() {
        return vanillaRegistry.getConfigurationHelper();
    }
}
