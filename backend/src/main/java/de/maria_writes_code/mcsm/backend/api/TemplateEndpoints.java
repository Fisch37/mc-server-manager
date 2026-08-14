package de.maria_writes_code.mcsm.backend.api;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplate;
import de.maria_writes_code.mcsm.backend.features.templates.ServerTemplateDefinition;
import de.maria_writes_code.mcsm.backend.features.templates.TemplateProvider;
import de.maria_writes_code.mcsm.backend.features.versions.VersionRegistry;

@RestController
@RequestMapping("templates")
public class TemplateEndpoints {
    @Autowired
    private TemplateProvider templates;
    @Autowired
    private VersionRegistry versions;

    @GetMapping("")
    public Stream<TemplateSummaryObject> getTemplateSummary() {
        return templates.getTemplates()
            .map(t -> new TemplateSummaryObject(t, versions));
    }

    @GetMapping("{id}")
    public TemplateSummaryObject getTemplate(@PathVariable String id) {
        return new TemplateSummaryObject(templates.getTemplate(id), versions);
    }

    public record TemplateSummaryObject(
        String id,
        String name,
        boolean has_mods,
        List<String> versions
    ) {
        public TemplateSummaryObject(ServerTemplate template, VersionRegistry versions) {
            this(template.getDefinition(), versions);
        }

        public TemplateSummaryObject(ServerTemplateDefinition template, VersionRegistry versions) {
            this(
                template.id(),
                template.name(),
                false,
                template.versions()
                    .getAllVersions(versions)
                    .map(v -> v.id())
                    .collect(Collectors.toList())
            );
        }
    }
}
