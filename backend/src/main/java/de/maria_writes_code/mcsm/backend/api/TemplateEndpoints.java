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

@RestController
@RequestMapping("templates")
public class TemplateEndpoints {
    @Autowired
    private TemplateProvider templates;

    @GetMapping("")
    public Stream<TemplateSummaryObject> getTemplateSummary() {
        return templates.getTemplates()
            .map(TemplateSummaryObject::new);
    }

    @GetMapping("{id}")
    public TemplateSummaryObject getTemplate(@PathVariable String id) {
        return new TemplateSummaryObject(templates.getTemplate(id));
    }

    public record TemplateSummaryObject(
        String id,
        boolean has_mods,
        List<String> versions
    ) {
        public TemplateSummaryObject(ServerTemplate template) {
            this(template.getDefinition());
        }

        public TemplateSummaryObject(ServerTemplateDefinition template) {
            this(
                template.id(),
                false,
                template.versions()
                    .stream()
                    .map(v -> v.id())
                    .collect(Collectors.toList())
            );
        }
    }
}
