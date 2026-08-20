# Server Template Specification
A template is a preset for creating a server for some set of supported versions.
It can be either a folder or a ZIP-Archive with a single [`template.xml`](#template-xml) file at its root, a [`files`](#files) folder, and optionally any number of [overlays](#overlays) in the `overlays` folder.

Symbolic links are allowed in templates so long as they do not exit the out of their container folder (e.g. `files` if the symlink is part of the normal file)

## Files
This is the basic structure of the server.
When a server is created, this folders contents will be recursively copied into the server folder.

## Overlays
These are folders under the `overlay` folder and referenced in the [`template.xml`](#template-xml) with a list of versions for which the overlay will be applied. Applicable ooverlays are applied in order of appearance, so that the last entry in the `<overlays>` node will have highest priority.

Contents of an overlay are structured identically to the base [files](#files) and any file that appears in an overlay will be copied into the server directory. If a file already exists at the destination, it is replaced completely. Folders will be merged together.

## template.xml
The following is an annotated example structure for a `template.xml` file.

```xml
<!--
    The id is used internally by the API and no two templates may use the same id.
    
    If present, the "abstract" property indicates that this template should not be publicly visible.
    This is useful if the template is used as a parent template and is not launchable on its own.
-->
<template
    id="example-template"
    abstract=""
    type="fabric"
>
    <!-- This is the name of the template that will be shown to users -->
    <name>Example Server Template</name>
    <!--
        If specified, this template will be applied before the current one.
        Note that if a parent is specified, the following options need not be provided:
            - executable
        If they are specified, they override the parent values,
        unless "inherit-executable" is set to true, in which case
        the executable of this template and its parent template are downloaded.
        Note that it is a logic error if multiple executables have the same name.
    -->
    <parent
        id="minecraft-vanilla"
        inherit-executable="true"
    />
    
    <executable file="server.jar">
        <terminator>
            <!-- The text to send on the command line -->
            <command>/stop</command>
            <!-- Terminate the process by sending SIGTERM -->
            <signal />
        </terminator>
        <argument>-Xmx2G</argument>
        <argument>-Xms2G</argument>
    </executable>
    <!--
        This is the optional list of overlays.
        If it is not present, no overlays are provided.
    -->
    <overlays>
        <!--
            The "location" property specifies the folder name for this overlay.
            The location must not include any / and cannot be composed solely of . characters.
        -->
        <overlay location="example-overlay">
            <!-- The versions for which this overlay applies -->
            <versions>
                <version>
                    <vanilla>1.21.11</vanilla>
                    <fabric-loader>0.16.11</fabric-loader>
                </version>
                <version>
                    <!--
                        Elided versions act as wildcards.
                        Therefore this would be
                        [vanilla=1.21.10, fabric-loader=*, fabric-installer=*]
                    -->
                    <vanilla>1.21.10</vanilla>
                </version>
                <!--
                Specifies a range of versions over the list of supported versions.
                The overlay is applied for all versions starting from the "first" version and ending with the "last" version (both inclusive).
                
                Either property may be left unspecified, in which case the version range expands to the limits of the supported versions list.
                -->
                <version-range>
                    <vanilla first="1.20.1" last="1.20.6" />
                </version-range>
            </versions>
        </overlay>
    </overlays>
</template>
```