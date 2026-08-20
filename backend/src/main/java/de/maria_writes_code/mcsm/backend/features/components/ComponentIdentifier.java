package de.maria_writes_code.mcsm.backend.features.components;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import de.maria_writes_code.mcsm.backend.utils.Utils;

public enum ComponentIdentifier {
    Vanilla("vanilla")
    ;

    private static final Map<String, ComponentIdentifier> NAME_MAP = Arrays.stream(ComponentIdentifier.values())
        .collect(Collectors.toUnmodifiableMap(c -> c.getSerializedName(), Function.identity()));
    private final String serialized;

    ComponentIdentifier(String serialized) {
        this.serialized = serialized;
    }

    @JsonValue
    public String getSerializedName() {
        return serialized;
    }

    @JsonCreator
    public static ComponentIdentifier fromSerializedName(String serialized) {
        return Utils.throwIfNull(
            NAME_MAP.get(serialized),
            () -> new IllegalArgumentException("String \"%s\" does not correspond to a VersionSource".formatted(serialized))
        );
    }
}
