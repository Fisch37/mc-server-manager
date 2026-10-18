package de.maria_writes_code.mcsm.backend.features.components;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class ComponentIdentifier {
    private final String id;

    @JsonCreator
    public ComponentIdentifier(String id) {
        this.id = id;
    }

    @JsonValue
    public String getSerializedName() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ComponentIdentifier id2
            && Objects.equals(id, id2.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
