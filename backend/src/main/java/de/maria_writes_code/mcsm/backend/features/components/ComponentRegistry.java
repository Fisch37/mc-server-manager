package de.maria_writes_code.mcsm.backend.features.components;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.maria_writes_code.mcsm.backend.utils.Utils;

@Service
public class ComponentRegistry implements InitializingBean {
    @Autowired
    private VanillaVersionRegistry vanilla;

    @Override
    public void afterPropertiesSet() throws Exception {
        Utils.requireNonNull(vanilla);
    }

    public ServerComponent<?> getComponent(ComponentIdentifier identifier) {
        switch (identifier) {
            case ComponentIdentifier.Vanilla:
                return vanilla;
            default:
                return null;
        }
    }
}
