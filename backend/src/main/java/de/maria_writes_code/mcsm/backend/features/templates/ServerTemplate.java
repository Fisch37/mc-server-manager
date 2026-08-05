package de.maria_writes_code.mcsm.backend.features.templates;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;

import org.apache.commons.io.FileUtils;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import de.maria_writes_code.mcsm.backend.features.versions.VersionRegistry;
import de.maria_writes_code.mcsm.backend.utils.Utils;

@NullMarked
public class ServerTemplate {
    private final static String
        TEMPLATE_DEF_LOC = "template.xml",
        TEMPLATE_FILES_LOC = "files",
        TEMPLATE_OVERLAY_LOC = "overlays"
        ;
    private final static XmlMapper TEMPLATE_READER = new XmlMapper();
    
    private final Context context;

    private final Path location;
    private final ServerTemplateDefinition definition;

    /**
     * Read a new template from the specified directory.
     * @param location The location of the template.
     * @throws IOException an I/O error occurred while reading the template definition.
     * @throws FileNotFoundException The template structure is invalid
     */
    public ServerTemplate(Context context, Path location) throws IOException {
        this.context = context;
        this.location = location;
        
        try (var template_file = new BufferedReader(
            new FileReader(location.resolve(TEMPLATE_DEF_LOC).toFile())
        )) {
            definition = TEMPLATE_READER.readValue(template_file, ServerTemplateDefinition.class);
        }

        if (!getFilesLocation().toFile().isDirectory()) {
            throw new FileNotFoundException("Template does not have a files directory");
        }
        for (var overlay : definition.overlays()) {
            if (!getOverlayLocation().resolve(overlay.location()).toFile().isDirectory()) {
                throw new FileNotFoundException(
                    "Overlay at %s does not exist".formatted(overlay.location())
                );
            }
        }
    }

    public ServerTemplateDefinition getDefinition() {
        return definition;
    }

    private Path getFilesLocation() {
        return location.resolve(TEMPLATE_FILES_LOC);
    }

    private Path getOverlayLocation() {
        return location.resolve(TEMPLATE_OVERLAY_LOC);
    }

    public void apply(Path path, String versionId) throws IOException {
        var parentTemplate = Optional.ofNullable(definition.parent())
            .map(parent -> parent.id())
            // TODO: Throw an error or warning when the parent template is specified, but does not exist
            .flatMap(parentId -> Optional.ofNullable(context.templateProvider.getTemplate(parentId)))
            .orElse(null);
        if (parentTemplate != null)
            parentTemplate.apply(path, versionId);

        FileUtils.copyDirectory(getFilesLocation().toFile(), path.toFile());
        for (var overlay : definition.overlays()) {
            if (
                overlay.versions()
                    .stream()
                    .anyMatch(v -> v.contains(versionId, definition.versions()))
            ) {
               var overlay_src = getOverlayLocation().resolve(overlay.location());
               FileUtils.copyDirectory(overlay_src.toFile(), path.toFile());
            }
        }

        try (var file = new FileOutputStream(path.resolve(definition.executable().file()).toFile())) {
            context.versionRegistry.getExecutable(versionId, file);
        }
    }

    @Component
    public static class Context implements InitializingBean {
        @Autowired
        private VersionRegistry versionRegistry;
        // @Autowired
        private TemplateProvider templateProvider;
        
        public void setTemplateProvider(TemplateProvider templateProvider) {
            this.templateProvider = templateProvider;
        }

        @Override
        public void afterPropertiesSet() throws Exception {
            Utils.requireNonNull(versionRegistry);
        }
    }
}
