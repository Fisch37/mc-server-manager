package de.maria_writes_code.mcsm.backend.features.runtimes;

import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

@Service @Scope("singleton")
public class RuntimeProvider {
    public @Nullable JavaRuntime getRuntime(int javaVersion) {
        throw new NotImplementedException();
    }

    public void fetchRuntime(int javaVersion) {
        throw new NotImplementedException();
    }
}
