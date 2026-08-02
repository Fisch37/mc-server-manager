package de.maria_writes_code.mcsm.backend.features.templates;

import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

@Service
public class TemplateProvider {
    public @Nullable ServerTemplate getTemplate(String id) {
        throw new NotImplementedException();
    }
}
