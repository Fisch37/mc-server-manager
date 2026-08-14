package de.maria_writes_code.mcsm.backend.features.versions;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import de.maria_writes_code.mcsm.backend.utils.Utils;

public enum VersionSource {
    Vanilla("vanilla")
    ;

    private static final Map<String, VersionSource> NAME_MAP = Arrays.stream(VersionSource.values())
        .collect(Collectors.toUnmodifiableMap(VersionSource::getSerializedName, Function.identity()));
    private final String serialized;

    VersionSource(String serialized) {
        this.serialized = serialized;
    }

    @JsonValue
    public String getSerializedName() {
        return serialized;
    }

    @JsonCreator
    public static VersionSource fromSerializedName(String serialized) {
        return Utils.throwIfNull(
            NAME_MAP.get(serialized),
            () -> new IllegalArgumentException("String \"%s\" does not correspond to a VersionSource".formatted(serialized))
        );
    }
}
