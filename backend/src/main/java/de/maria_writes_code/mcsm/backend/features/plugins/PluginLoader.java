package de.maria_writes_code.mcsm.backend.features.plugins;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import de.maria_writes_code.mcsm.backend.AppConfig;
import de.maria_writes_code.mcsm.backend.features.components.ComponentRegistry;
import de.maria_writes_code.mcsm.backend.utils.Utils;

@Service
public class PluginLoader implements InitializingBean {
    private static final String PLUGIN_CONFIG_RESOURCE = "mcsm-plugin.json";
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginLoader.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Autowired
    private AppConfig config;
    @Autowired 
    private ComponentRegistry serverTypeRegistry;

    private List<PluginServerType<?>> pluginServerTypes;
    private List<PluginEntrypoint> pluginEntrypoints;

    @Override
    public void afterPropertiesSet() throws IOException {
        this.pluginServerTypes = new ArrayList<>();
        this.pluginEntrypoints = new ArrayList<>();
        for (
            var location
            : Utils.toIterator(
                Files.list(config.getPluginsLocation())
                    .filter(p -> p.getFileName().toString().endsWith(".jar"))
            )
        ) {
            PluginData loadedData;
            try {
                loadedData = loadPlugin(location);
            } catch (Exception e) {
                LOGGER.error("Failed to load plugin {}", location.getFileName(), e);
                continue;
            }
            pluginServerTypes.addAll(loadedData.serverTypes);
            pluginEntrypoints.addAll(loadedData.entrypoints);
        }

        serverTypeRegistry.addComponents(pluginServerTypes);
        for (var entrypoint : pluginEntrypoints) {
            try {
                entrypoint.onInitialized(createContext());
            } catch (Exception e) {
                LOGGER.error("Exception occured while initializing entrypoint {}", entrypoint.getClass().getName(), e);
            }
        }
        for (var serverType : this.pluginServerTypes) {
            try {
                serverType.onInitialized(createContext());
            } catch (Exception e) {
                LOGGER.error("Exception occured while initializing server type {}", serverType.getClass().getName(), e);
            }
        }
    }

    private PluginContext createContext() {
        return new PluginContext() {
            @Override
            public ServerTypeRegistry getServerTypeRegistry() {
                return serverTypeRegistry;
            }

            @Override
            public Config getAppConfig() {
                return config;
            }
        };
    }

    private PluginData loadPlugin(Path location) throws Exception {
        try(var loader = new URLClassLoader(new URL[] { location.toUri().toURL() })) {
            PluginConfig config;
            try(var configFile = loader.getResourceAsStream(PLUGIN_CONFIG_RESOURCE)) {
                config = MAPPER.readValue(configFile, PluginConfig.class);
            }

            var entrypoints = new ArrayList<PluginEntrypoint>(config.entrypoints().size());
            for (var classPath : config.entrypoints()) {
                var clazz = loader.loadClass(classPath)
                    .asSubclass(PluginEntrypoint.class);
                try {
                    entrypoints.add(
                        clazz.getConstructor()
                            .newInstance()
                    );
                } catch (InstantiationException e) {
                    LOGGER.error(
                        "Failed to load entry point {} for plugin {}",
                        clazz.getName(),
                        config.name(),
                        e
                    );
                }
            }

            var serverTypes = new ArrayList<PluginServerType<?>>(config.server_types().size());
            for (var classPath : config.server_types()) {
                var clazz = loader.loadClass(classPath)
                    .asSubclass(PluginServerType.class);
                try {
                    serverTypes.add(
                        clazz.getConstructor()
                            .newInstance()
                    );
                } catch (InstantiationException e) {
                    LOGGER.error(
                        "Failed to initialize server type {} for plugin {}",
                        classPath,
                        config.name(),
                        e
                    );
                }
            }
            
            return new PluginData(
                config.name(),
                entrypoints,
                serverTypes
            );
        }
    }

    private record PluginData(String name, List<PluginEntrypoint> entrypoints, List<PluginServerType<?>> serverTypes) { }
}
