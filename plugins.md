# Plugin Specification
MCSM includes (or will include) support for plugins.
Plugins are JAR files loaded at runtime by the plugin loader, which may contain additional server types and surrounding code.

Plugins must be placed in `$MCSM_DATA_PATH/plugins` as jar files.

## Structure
A plugin must include a [`mcsm-plugin.json`](#plugin-config) resource with the format specified below. Only server types referenced in this config will be loaded.

Any server types added must implement the `PluginServerType` interface and have a zero-argument constructor, which may throw any exception. MCSM will construct one instance of all specified server types.

### Plugin Config
```json
{
    "version": 1,
    "name": <string>,
    "entrypoints": [
        <a class binary name>*
    ],
    "server_types": [
        <a class binary name>*
    ]
}
```

## Entrypoints
Plugins can also specify entrypoints, which must implement the `PluginEntrypoint` interface and have a zero-arguments constructor, which may throw any exception. Entrypoints are intended to allow for execution of arbitrary plugin code, but their `PluginEntrypoint#onInitialized` will be executed in a blocking manner so any long-running tasks which needn't be completed for the server to launch should be scheduled asynchronously.

## Loading
The order in which plugins load is unspecified, but a plugin will finish loading before any other plugin is loaded.

Entrypoints will be loaded before server types and in the order specified by the config. After all plugins have finished loading `PluginEntrypoints#onInitialized` will be called on all entrypoints in the order in which they were loaded.

Plugin server types will be loaded in the order specified in the config. After all plugins have finished loading `PluginServerType#onInitialized` will be called on each loaded server type. Note that if the constructor of a server type threw an exception, loading will still proceed, but that server type won't be available.
