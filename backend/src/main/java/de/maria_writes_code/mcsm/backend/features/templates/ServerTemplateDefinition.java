package de.maria_writes_code.mcsm.backend.features.templates;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import de.maria_writes_code.mcsm.backend.features.components.ComponentIdentifier;
import de.maria_writes_code.mcsm.backend.features.components.VersionCombo;
import de.maria_writes_code.mcsm.backend.features.components.versions.VersionProviderCollection;
import de.maria_writes_code.mcsm.backend.utils.Utils;

@JacksonXmlRootElement(localName = "template")
@JsonInclude(Include.NON_NULL)
@NullMarked
public record ServerTemplateDefinition(
    @JacksonXmlProperty(isAttribute = true)
    String id,
    String name,
    @JacksonXmlProperty(localName = "abstract", isAttribute = true)
    boolean isAbstract,
    @JacksonXmlProperty(isAttribute = true)
    ComponentIdentifier type,
    @Nullable Parent parent,
    @Nullable Executable executable,
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    List<Overlay> overlays
) {
    public ServerTemplateDefinition {
        Utils.throwIfNullMany(
            () -> new IllegalArgumentException("id, name, components, and overlays cannot be null"),
            id, name, overlays
        );
        if (parent == null && executable  == null)
            throw new IllegalArgumentException("base templates must specify an executable");
    }

    /**
     * Returns a stream of this template and all its parents in ascending order.
     */
    public Stream<ServerTemplateDefinition> getHierarchy(TemplateProvider templateProvider) {
        ServerTemplate parentTemplate;
        return Stream.concat(
            Stream.of(this),
            parent == null
                ? Stream.empty()
                : (
                    (parentTemplate = templateProvider.getTemplate(parent.id)) == null
                    ? Stream.empty()
                    : parentTemplate.getDefinition().getHierarchy(templateProvider)
                )
        );
    }

    @JacksonXmlRootElement(localName = "parent")
    public record Parent(
        @JacksonXmlProperty(isAttribute = true)
        String id,
        @JacksonXmlProperty(localName = "inherit-executable", isAttribute = true)
        boolean inheritExecutable
    ) { }

    @JacksonXmlRootElement(localName = "executable")
    public record Executable(
        @JacksonXmlProperty(isAttribute = true)
        Path file,
        Terminator terminator,
        @JacksonXmlElementWrapper(useWrapping = false)
        @JacksonXmlProperty(localName = "argument")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        ArrayList<String> arguments
    ) {
        public Executable {
            Utils.throwIfNullMany(
                () -> new IllegalArgumentException("template executable must have a file and a terminator"),
                file, terminator
            );
            checkDoesNotEscapeRoot(file, "executable location");
        }
    }
    
    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({
        @Type(value = ServerTemplateDefinition.CommandTerminator.class),
        @Type(value = ServerTemplateDefinition.SignalTerminator.class)
    })
    public sealed interface Terminator permits CommandTerminator, SignalTerminator { }

    public record CommandTerminator(
        @JacksonXmlProperty(localName = "command")
        String command
    ) implements Terminator { }

    public record SignalTerminator(String signal) implements Terminator { }
    
    public record Overlay(
        @JacksonXmlProperty(isAttribute = true)
        Path location,
        @JsonSetter(nulls = Nulls.FAIL)
        ArrayList<VersionRangeSpecifier> versions
    ) {
        public Overlay {
            checkDoesNotEscapeRoot(location, "overlay location");
        }
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
    @JsonSubTypes({
        @Type(value = ServerTemplateDefinition.Version.class),
        @Type(value = ServerTemplateDefinition.VersionRange.class)
    })
    public static interface VersionRangeSpecifier {
        boolean contains(VersionCombo version, VersionProviderCollection versionProviders);
    }

    @JacksonXmlRootElement(localName = "version")
    public record Version(
        @JsonAnyGetter @JsonAnySetter
        Map<String, String> values
    ) implements VersionRangeSpecifier, VersionCombo {
        @Override
        public boolean contains(VersionCombo version, VersionProviderCollection versionProviders) {
            return values.entrySet()
                .stream()
                .allMatch(entry ->
                    // check if constraints match this version
                    version.getVersion(entry.getKey())
                        .map(vId -> vId.equals(entry.getValue()))
                        // if no matching version exists, cannot be a match
                        .orElse(false)
                );
        }

        @Override
        public Map<String, String> getVersions() {
            return Collections.unmodifiableMap(values);
        }

        @Override
        public Optional<String> getVersion(String versionSourceId) {
            return Optional.ofNullable(values.get(versionSourceId));
        }
    }

    @JacksonXmlRootElement(localName = "version-range")
    public record VersionRange(
        @JsonAnyGetter @JsonAnySetter
        Map<String, RangedVersionId> ranges
    ) implements VersionRangeSpecifier {
        @Override
        public boolean contains(VersionCombo version, VersionProviderCollection versionProviders) {
            return ranges.entrySet()
                .stream()
                .allMatch(entry -> {
                    var provider = versionProviders.getVersionProvider(entry.getKey());
                    if (provider == null)
                        return false;
                    int testIdx = version.getVersion(entry.getKey())
                        .map(vId -> indexOf(entry.getKey(), vId, versionProviders))
                        .orElse(-1);
                    if (testIdx == -1)
                        return false;
                    int firstIdx = indexOf(entry.getKey(), entry.getValue().first, versionProviders);
                    int lastIdx = indexOf(entry.getKey(), entry.getValue().last, versionProviders);
                    return firstIdx >= testIdx && testIdx <= lastIdx;
                });
        }

        private static int indexOf(String key, String value, VersionProviderCollection versionProviders) {
            var provider = versionProviders.getVersionProvider(key);
            if (provider == null) {
                return -1;
            } else {
                return provider.indexOf(value);
            }
        }

        public record RangedVersionId(
            @JacksonXmlProperty(isAttribute = true)
            String first,
            @JacksonXmlProperty(isAttribute = true)
            String last
        ) { }
    }

    private static void checkDoesNotEscapeRoot(Path path, String fieldName) {
        if (path.isAbsolute()) {
            throw new IllegalArgumentException("%s must be relative and stay within the server root".formatted(fieldName));
        }

        var normalized = path.normalize();
        // TODO: Doing string comparison is not a tidy way to do this
        if (normalized.getNameCount() > 0 && "..".equals(normalized.getName(0).toString())) {
            throw new IllegalArgumentException("%s must not escape the server root".formatted(fieldName));
        }
    }

}
