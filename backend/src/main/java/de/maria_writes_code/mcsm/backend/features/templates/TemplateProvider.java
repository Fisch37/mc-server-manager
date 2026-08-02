package de.maria_writes_code.mcsm.backend.features.templates;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import de.maria_writes_code.mcsm.backend.CustomAppConfig;

@Service @Scope("singleton")
public class TemplateProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateProvider.class);

    @Autowired
    private CustomAppConfig config;

    private final ConcurrentMap<String, ServerTemplate> templates;

    public TemplateProvider() throws IOException {
        templates = new ConcurrentHashMap<>();
        var files = config.getTemplateLocation().toFile().listFiles();
        if (files == null) {
            throw new IOException("Templates files are not available");
        }
        for (var templateDir : files) {
            if (!templateDir.isDirectory())
                continue;
            var template = new ServerTemplate(templateDir.toPath());
            var replacedTemplate = templates.put(template.getDefinition().id(), template);
            if (replacedTemplate != null) {
                LOGGER.warn(
                    "Duplicate template for id {} ({} replaced {})",
                    replacedTemplate.getDefinition(),
                    template.getDefinition().name(),
                    replacedTemplate.getDefinition().name()
                );
            }
        }
    }

    public @Nullable ServerTemplate getTemplate(String id) {
        return templates.get(id);
    }
}
