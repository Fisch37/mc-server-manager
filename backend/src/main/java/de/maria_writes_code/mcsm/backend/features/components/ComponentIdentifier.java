package de.maria_writes_code.mcsm.backend.features.components;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public record ComponentIdentifier(@JsonValue String id) {
    @JsonCreator
    public ComponentIdentifier { }
}
