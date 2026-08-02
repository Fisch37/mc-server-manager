package de.maria_writes_code.api;

import java.util.stream.Stream;

import org.apache.commons.lang3.NotImplementedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("templates")
public class TemplateEndpoints {
    @GetMapping("/")
    public Stream<TemplateSummaryObject> getTemplateSummary() {
        throw new NotImplementedException();
    }

    public record TemplateSummaryObject() { }
}
