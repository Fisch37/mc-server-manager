package de.maria_writes_code.mcsm.backend.features.templates;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import de.maria_writes_code.mcsm.backend.CustomAppConfig;

@Service @Scope("singleton")
public class TemplateProvider implements InitializingBean {
    private static final Logger LOGGER = LoggerFactory.getLogger(TemplateProvider.class);

    @Autowired
    private CustomAppConfig config;
    @Autowired
    private ServerTemplate.Context templateContext;

    private ConcurrentMap<String, ServerTemplate> templates;

    public TemplateProvider() { }

    @Override
    public void afterPropertiesSet() throws IOException {
        // Resolves an otherwise circular reference
        templateContext.setTemplateProvider(this);

        templates = new ConcurrentHashMap<>();
        var files = Files.list(config.getTemplateLocation())
            .filter(f -> Files.isDirectory(f, LinkOption.NOFOLLOW_LINKS))
        ;
        // Converting the Stream#iterator method into an Iterable instance,
        // because Iterable<T> is a functional interface. This is so cursed.
        for (var templateDir : (Iterable<Path>)files::iterator) {
            var template = new ServerTemplate(templateContext, templateDir);
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

    public Stream<ServerTemplate> getTemplates() {
        return templates.values().stream();
    }
}
