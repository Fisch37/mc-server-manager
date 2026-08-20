package de.maria_writes_code.mcsm.backend.api;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.maria_writes_code.mcsm.backend.features.components.ComponentRegistry;
import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProvider;
import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplate;
import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplateDefinition;
import de.maria_writes_code.mcsm.backend.features.templates.TemplateProvider;
import de.maria_writes_code.mcsm.backend.features.components.versions.Version;
import de.maria_writes_code.mcsm.backend.utils.Utils;

@RestController
@RequestMapping("templates")
public class TemplateEndpoints {
    @Autowired
    private TemplateProvider templates;
    @Autowired
    TemplateSummaryObject.Context templateSummaryContext;

    @GetMapping("")
    public Stream<TemplateSummaryObject> getTemplateSummary() {
        return templates.getTemplates()
            .map(t -> new TemplateSummaryObject(t, templateSummaryContext));
    }

    @GetMapping("{id}")
    public TemplateSummaryObject getTemplate(@PathVariable String id) {
        return new TemplateSummaryObject(templates.getTemplate(id), templateSummaryContext);
    }

    public record TemplateSummaryObject(
        String id,
        String name,
        boolean has_mods,
        List<VersionSourceObject> versions
    ) {
        public TemplateSummaryObject(
            ServerTemplate template,
            Context context
        ) {
            this(template.getDefinition(), context);
        }

        public TemplateSummaryObject(
            ServerTemplateDefinition template,
            Context context
        ) {
            this(
                template.id(),
                template.name(),
                false,
                template.getHierarchy(context.templates)
                   .map(t -> context.components.getComponent(t.type()))
                   .flatMap(c -> c.getVersionProviders().stream())
                   .map(VersionSourceObject::new)
                   .collect(Collectors.toList())
            );
        }

        @Component
        public static class Context {
            @Autowired
            private TemplateProvider templates;
            @Autowired
            private ComponentRegistry components;
        }
    }

    public record VersionSourceObject(
        String source_id,
        String friendly_name,
        List<VersionInfoObject> versions
    ) {
        public VersionSourceObject(VersionProvider versionProvider) {
            this(
                versionProvider.getSourceIdentifier(),
                versionProvider.getFriendlyName(),
                versionProvider.getVersions()
                    .stream()
                    .map(VersionInfoObject::new)
                    .collect(Collectors.toList())
            );
        }
    }

    public record VersionInfoObject(
        String id,
        String channel
    ) {
        public final static String DEFAULT_CHANNEL = "release";

        public VersionInfoObject(Version version) {
            this(version.id(), Utils.or(version.channel(), DEFAULT_CHANNEL));
        }
    }
}
